package ru.danny.logging.utils;

import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.danny.logging.utils.LoggingConstants.ARRAY_DELIMITER;
import static ru.danny.logging.utils.LoggingConstants.HEADER_FORMAT;

@UtilityClass
public class CommonUtils {

	public static Set<String> headersStringToSet(String headersString) {
		return Arrays.stream(headersString.split(","))
			.map(String::trim)
			.map(String::toLowerCase)
			.collect(Collectors.toSet());
	}

	public static <K, V> String getHeadersString(Stream<Map.Entry<K, V>> headersStream) {
		return headersStream
			.map(header -> String.format(HEADER_FORMAT, header.getKey(), header.getValue()))
			.collect(Collectors.joining(ARRAY_DELIMITER));
	}

}
