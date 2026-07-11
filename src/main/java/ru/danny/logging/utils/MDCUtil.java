package ru.danny.logging.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
import org.slf4j.MDC;
import org.springframework.web.util.ContentCachingResponseWrapper;

@UtilityClass
public class MDCUtil {

	private static final String REQUEST_METHOD = "requestMethod";
	private static final String REQUEST_URI = "requestUri";
	private static final String CONTENT_LENGTH = "contentLength";
	private static final String STATUS = "status";
	private static final String RESPONSE_LENGTH = "responseLength";
	private static final String RESPONSE_TIME = "responseTime";

	public static void putRequestDataToMDC(HttpServletRequest request) {
		MDC.put(REQUEST_METHOD, request.getMethod());
		MDC.put(REQUEST_URI, request.getRequestURI());
		MDC.put(CONTENT_LENGTH, String.valueOf(request.getContentLength()));
	}

	public static void putResponseDataToMDC(ContentCachingResponseWrapper response, long responseTime) {
		MDC.put(STATUS, String.valueOf(response.getStatus()));
		MDC.put(RESPONSE_TIME, String.valueOf(responseTime));
		MDC.put(RESPONSE_LENGTH, String.valueOf(response.getContentSize()));
	}

	public static void clearMDC() {
		MDC.remove(REQUEST_METHOD);
		MDC.remove(REQUEST_URI);
		MDC.remove(CONTENT_LENGTH);
		MDC.remove(STATUS);
		MDC.remove(RESPONSE_LENGTH);
		MDC.remove(RESPONSE_TIME);
	}

}
