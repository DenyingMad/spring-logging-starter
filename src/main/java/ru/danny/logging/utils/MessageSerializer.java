package ru.danny.logging.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;

import static ru.danny.logging.utils.LoggingConstants.NO_BODY;
import static ru.danny.logging.utils.LoggingConstants.SERIALIZATION_ERROR;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MessageSerializer {

	public static String getMessageBody(ObjectMapper objectMapper, Object object) {
		return getMessageBody(objectMapper, object, Integer.MAX_VALUE);
	}

	public static String getMessageBody(ObjectMapper objectMapper, Object object, int maxLength) {
		if (object == null) {
			return NO_BODY;
		}
		if (object instanceof String) {
			return BodyLogUtils.truncate((String) object, maxLength);
		}
		if (object instanceof byte[]) {
			try {
				object = objectMapper.readValue((byte[]) object, Object.class);
			} catch (IOException e) {
				return SERIALIZATION_ERROR;
			}
		}
		try {
			return BodyLogUtils.truncate(objectMapper.writeValueAsString(object), maxLength);
		} catch (JsonProcessingException e) {
			return SERIALIZATION_ERROR;
		}
	}

}
