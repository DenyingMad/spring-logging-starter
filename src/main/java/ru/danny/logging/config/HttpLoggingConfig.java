package ru.danny.logging.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import ru.danny.logging.http.HttpLogger;
import ru.danny.logging.utils.CommonUtils;

import java.util.regex.Pattern;

@Configuration
@ConditionalOnExpression("${logging.request.enabled:true}")
public class HttpLoggingConfig {

	@Value("${logging.request.regex:.*}")
	private String loggingRequestRegex;
	@Value("${logging.request.exclude-headers:authorization, authorization-authdata}")
	private String loggingRequestExcludeHeaders;

	@Bean
	@Order(value = Ordered.HIGHEST_PRECEDENCE + 1) // Order after EntityTraceWebFilter
	public HttpLogger requestLoggingFilter() {
		var excludeHeaders = CommonUtils.headersStringToSet(loggingRequestExcludeHeaders);
		return new HttpLogger(Pattern.compile(loggingRequestRegex), excludeHeaders);
	}

}
