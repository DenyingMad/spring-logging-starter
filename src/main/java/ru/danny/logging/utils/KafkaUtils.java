package ru.danny.logging.utils;

import lombok.experimental.UtilityClass;
import org.apache.kafka.common.header.Headers;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@UtilityClass
public class KafkaUtils {

	public String getHeadersString(Headers headers) {
		return StreamSupport.stream(headers.spliterator(), false)
			.map(header -> header.key() + ": " + new String(header.value()) + " ")
			.collect(Collectors.joining());
	}

}
