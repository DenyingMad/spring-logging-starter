package ru.danny.logging.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.stream.messaging.DirectWithAttributesChannel;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;

import java.util.Objects;
import java.util.Optional;

import static ru.danny.logging.utils.ChannelTypeUtils.INPUT;

@Slf4j
@RequiredArgsConstructor
public class IncomingMessagingLogger implements ChannelInterceptor {

	private final MessagingLogger messagingLogger;

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		if (channel instanceof DirectWithAttributesChannel) {
			var directChannel = (DirectWithAttributesChannel) channel;
			var channelType = getChannelType(directChannel);
			if (INPUT.equals(channelType)) {
				messagingLogger.logMessage(log, message, directChannel.getBeanName(), channelType);
			}
		}
		return message;
	}

	private String getChannelType(DirectWithAttributesChannel channel) {
		return Optional.ofNullable(channel.getAttribute("type"))
			.map(Objects::toString)
			.orElse(StringUtils.EMPTY);
	}

}
