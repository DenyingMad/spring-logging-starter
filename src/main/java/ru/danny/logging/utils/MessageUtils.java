package ru.danny.logging.utils;

import lombok.experimental.UtilityClass;
import org.springframework.messaging.MessageHeaders;

@UtilityClass
public class MessageUtils {

	public static String getHeadersString(MessageHeaders headers) {
		var headersStream = headers.entrySet().stream();
		return CommonUtils.getHeadersString(headersStream);
	}

}
