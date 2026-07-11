package ru.danny.logging.config;

import net.devh.boot.grpc.client.interceptor.GrpcGlobalClientInterceptor;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Configuration;
import ru.danny.logging.grpc.GrpcClientLogger;
import ru.danny.logging.grpc.GrpcServerLogger;

@Configuration
@ConditionalOnExpression("${logging.grpc.enabled:true}")
public class GrpcLoggingConfig {

	@Value("${logging.grpc.max-length:1000}")
	private int messageMaxLength;

	@GrpcGlobalClientInterceptor
	public GrpcClientLogger logClientInterceptor() {
		return new GrpcClientLogger(messageMaxLength);
	}

	@GrpcGlobalServerInterceptor
	public GrpcServerLogger logServerInterceptor() {
		return new GrpcServerLogger(messageMaxLength);
	}

}
