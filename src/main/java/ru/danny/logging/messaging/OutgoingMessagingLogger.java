package ru.danny.logging.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.stream.messaging.DirectWithAttributesChannel;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static ru.danny.logging.utils.ChannelTypeUtils.OUTPUT;

@Slf4j
@RequiredArgsConstructor
public class OutgoingMessagingLogger implements ChannelInterceptor {

	private static final String TARGET_PROTOCOL_HEADER = "target-protocol";
	private static final Set<String> TARGET_PROTOCOLS_FOR_LOGGING = Set.of("streamBridge", "kafka");

	private final MessagingLogger messagingLogger;

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		if (channel instanceof DirectWithAttributesChannel) {
			var directChannel = (DirectWithAttributesChannel) channel;
			var channelType = getChannelType(directChannel);
			if (isStreamBridgeMessage(channelType, message)) {
				var topicName = getStreamBridgeTopicName(directChannel);
				messagingLogger.logMessageOutgoingWithTopicName(log, message, topicName);
			} else if (OUTPUT.equals(channelType)) {
				messagingLogger.logMessage(log, message, directChannel.getBeanName(), channelType);
			}
		}
		return message;
	}

	private boolean isStreamBridgeMessage(String channelType, Message<?> message) {
		var targetProtocol = message.getHeaders().get(TARGET_PROTOCOL_HEADER);
		return StringUtils.EMPTY.equals(channelType) && Objects.nonNull(targetProtocol) && TARGET_PROTOCOLS_FOR_LOGGING.contains(targetProtocol);
	}

	private String getStreamBridgeTopicName(DirectWithAttributesChannel channel) {
		var topicName = getFieldByPath(channel, "dispatcher.theOneHandler.delegate.topicExpression.literalValue");
		return topicName instanceof String name ? name : null;
	}

	private String getChannelType(DirectWithAttributesChannel channel) {
		return Optional.ofNullable(channel.getAttribute("type"))
			.map(Objects::toString)
			.orElse(StringUtils.EMPTY);
	}

	private Object getFieldByPath(Object bean, String fieldPath) {
		try {
			String[] nestedFields = StringUtils.split(fieldPath, ".");
			Class<?> componentClass = bean.getClass();
			Object value = bean;
			for (String nestedField : nestedFields) {
				Field field = ReflectionUtils.findField(componentClass, nestedField);
				if (field != null) {
					field.setAccessible(true);
					value = ReflectionUtils.getField(field, value);
					if (value != null) {
						componentClass = value.getClass();
					}
				}
			}
			return value;
		} catch (Exception e) {
			return StringUtils.EMPTY;
		}
	}

}
