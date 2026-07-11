package ru.danny.logging.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.cloud.stream.binder.Binder;
import org.springframework.cloud.stream.config.BindingServiceProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.config.GlobalChannelInterceptor;
import ru.danny.logging.messaging.IncomingMessagingLogger;
import ru.danny.logging.messaging.MessagingLogger;
import ru.danny.logging.messaging.OutgoingMessagingLogger;

@Configuration
@ConditionalOnClass({Binder.class})
public class MessagingLoggingConfig {

	@Bean
	public MessagingLogger messagingLogger(ObjectMapper objectMapper,
		ApplicationContext applicationContext) {
		var bindingServiceProperties = applicationContext.getBean(BindingServiceProperties.class);
		return new MessagingLogger(objectMapper, bindingServiceProperties);
	}

	@Bean
	@ConditionalOnExpression("${logging.messaging.outgoing.enabled:true}")
	@GlobalChannelInterceptor(order = Integer.MIN_VALUE)
	public OutgoingMessagingLogger messageOutgoingLogger(MessagingLogger messagingLogger) {
		return new OutgoingMessagingLogger(messagingLogger);
	}

	@Bean
	@ConditionalOnExpression("${logging.messaging.incoming.enabled:true}")
	@GlobalChannelInterceptor(order = Integer.MIN_VALUE)
	public IncomingMessagingLogger messageIncomingLogger(MessagingLogger messagingLogger) {
		return new IncomingMessagingLogger(messagingLogger);
	}

}
