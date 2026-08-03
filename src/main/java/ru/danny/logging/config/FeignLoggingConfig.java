package ru.danny.logging.config;

import feign.Feign;
import feign.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.danny.logging.feign.FeignLogger;
import ru.danny.logging.utils.CommonUtils;

@Configuration
@ConditionalOnClass({Feign.class})
@ConditionalOnExpression("${logging.feign.enabled:true}")
public class FeignLoggingConfig {

	@Value("${logging.feign.exclude-headers:authorization, authorization-authdata}")
	private String loggingFeignExcludeHeaders;
	@Value("${logging.feign.max-length:1000}")
	private int messageMaxLength;

	@Bean
	Logger.Level feignLoggerLevel() {
		return Logger.Level.FULL;
	}

	@Bean
	public Logger feignLogger() {
		var excludeHeaders = CommonUtils.headersStringToSet(loggingFeignExcludeHeaders);
		return new FeignLogger(excludeHeaders, messageMaxLength);
	}

}
