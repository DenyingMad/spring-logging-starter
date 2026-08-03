package ru.danny.logging.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.springframework.cloud.stream.config.BindingServiceProperties;
import org.springframework.messaging.Message;
import ru.danny.logging.utils.MessageSerializer;
import ru.danny.logging.utils.MessageUtils;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.danny.logging.utils.ChannelTypeUtils.INPUT;
import static ru.danny.logging.utils.ChannelTypeUtils.OUTPUT;
import static ru.danny.logging.utils.LoggingConstants.UNKNOWN_DESTINATION;

public class MessagingLogger {

	private final ObjectMapper objectMapper;
	private final Map<String, String> channelToDestinationMap;
	private final int maxLength;

	public MessagingLogger(ObjectMapper objectMapper, BindingServiceProperties bindingServiceProperties, int maxLength) {
		this.objectMapper = objectMapper;
		this.channelToDestinationMap = bindingServiceProperties.getBindings().entrySet().stream()
			.collect(Collectors.toMap(Map.Entry::getKey, binding -> binding.getValue().getDestination()));
		this.maxLength = maxLength;
	}

	public void logMessageIncoming(Logger log, Message<?> message, String channelName) {
		logMessage(log, message, channelName, INPUT);
	}

	public void logMessageOutgoingWithTopicName(Logger log, Message<?> message, String topicName) {
		logMessage(log, message, null, topicName, OUTPUT);
	}

	public void logMessage(Logger log, Message<?> message, String channelName, String channelType) {
		logMessage(log, message, channelName, null, channelType);
	}

	private void logMessage(Logger log, Message<?> message, String channelName, String topicName, String channelType) {
		var prefix = INPUT.equals(channelType)
			? "<== Incoming message"
			: "==> Outgoing message";
		var topic = topicName != null
			? topicName
			: getDestination(channelName);
		var headers = MessageUtils.getHeadersString(message.getHeaders());
		var body = MessageSerializer.getMessageBody(objectMapper, message.getPayload(), maxLength);
		log.info("{}, destination = {},\nheaders = [{}],\nbody = {}", prefix, topic, headers, body);
	}

	private String getDestination(String channelName) {
		return Optional.ofNullable(channelName).map(channelToDestinationMap::get).orElse(UNKNOWN_DESTINATION);
	}

}
