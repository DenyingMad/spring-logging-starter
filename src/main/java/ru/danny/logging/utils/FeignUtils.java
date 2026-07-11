package ru.danny.logging.utils;

import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static feign.Util.valuesOrEmpty;

@UtilityClass
public class FeignUtils {

	public static String getHeadersString(Map<String, Collection<String>> headers, Set<String> excludeHeaders) {
		var headersStream = headers.keySet().stream()
			.filter(key -> !excludeHeaders.contains(key.toLowerCase()))
			.flatMap(key -> valuesOrEmpty(headers, key).stream().map(value -> Map.entry(key, value)));
		return CommonUtils.getHeadersString(headersStream);
	}

}
