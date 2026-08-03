package ru.danny.logging.utils;

import lombok.experimental.UtilityClass;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static ru.danny.logging.utils.LoggingConstants.BINARY_DATA;
import static ru.danny.logging.utils.LoggingConstants.NO_BODY;

@UtilityClass
public class BodyLogUtils {

	private static final Set<String> BINARY_MEDIA_TYPES = Set.of(
		"application/octet-stream",
		"application/pdf",
		"application/zip",
		"application/gzip",
		"application/x-gzip",
		"application/x-zip-compressed",
		"application/x-7z-compressed",
		"application/x-rar-compressed",
		"application/x-tar",
		"application/java-archive",
		"application/protobuf",
		"application/x-protobuf"
	);

	private static final Set<String> PLACEHOLDERS = Set.of(
		NO_BODY,
		BINARY_DATA,
		LoggingConstants.SERIALIZATION_ERROR,
		LoggingConstants.UNKNOWN_DESTINATION,
		LoggingConstants.UNKNOWN_STATUS
	);

	public static String truncate(String body, int maxLength) {
		if (body == null || body.isEmpty() || PLACEHOLDERS.contains(body) || maxLength <= 0) {
			return body;
		}
		return body.substring(0, Math.min(body.length(), maxLength));
	}

	public static boolean isBinaryContentType(String contentType) {
		if (contentType == null || contentType.isBlank()) {
			return false;
		}
		String mediaType = contentType.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
		if (mediaType.startsWith("image/") || mediaType.startsWith("video/") || mediaType.startsWith("audio/") || mediaType.startsWith("multipart/")) {
			return true;
		}
		return BINARY_MEDIA_TYPES.contains(mediaType);
	}

	public static String formatBody(String body, String contentType, int maxLength) {
		if (isBinaryContentType(contentType)) {
			return BINARY_DATA;
		}
		if (body == null || body.isEmpty()) {
			return NO_BODY;
		}
		return truncate(body, maxLength);
	}

	public static String formatBody(byte[] body, String contentType, Charset charset, int maxLength) {
		if (isBinaryContentType(contentType)) {
			return BINARY_DATA;
		}
		if (body == null || body.length == 0) {
			return NO_BODY;
		}
		Charset encoding = charset != null ? charset : StandardCharsets.UTF_8;
		return truncate(new String(body, encoding), maxLength);
	}

	public static String extractContentType(Map<String, Collection<String>> headers) {
		if (headers == null || headers.isEmpty()) {
			return null;
		}
		for (Map.Entry<String, Collection<String>> entry : headers.entrySet()) {
			if (entry.getKey() != null && "content-type".equalsIgnoreCase(entry.getKey())) {
				Collection<String> values = entry.getValue();
				if (values != null && !values.isEmpty()) {
					return values.iterator().next();
				}
			}
		}
		return null;
	}

}
