package ru.danny.logging.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.experimental.UtilityClass;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServletServerHttpRequest;

import java.util.List;
import java.util.Map;
import java.util.Set;

@UtilityClass
public class HttpUtils {

	public static String getHeadersString(HttpServletRequest request, Set<String> excludeHeaders) {
		HttpHeaders headers = new ServletServerHttpRequest(request).getHeaders();
		return getHeadersString(headers, excludeHeaders);
	}

	public static String getHeadersString(HttpServletResponse response, Set<String> excludeHeaders) {
		HttpHeaders headers = response.getHeaderNames().stream()
			.collect(HttpHeaders::new, (h, k) -> h.addAll(k, List.copyOf(response.getHeaders(k))), HttpHeaders::addAll);
		return getHeadersString(headers, excludeHeaders);
	}

	public static String getHeadersString(HttpHeaders headers, Set<String> excludeHeaders) {
		var headersStream = headers.keySet().stream()
			.filter(key -> !excludeHeaders.contains(key.toLowerCase()))
			.flatMap(key -> headers.getValuesAsList(key).stream().map(value -> Map.entry(key, value)));
		return CommonUtils.getHeadersString(headersStream);
	}

}
