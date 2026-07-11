package ru.danny.logging.http;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;
import ru.danny.logging.utils.HttpUtils;
import ru.danny.logging.utils.MDCUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import static ru.danny.logging.utils.LoggingConstants.BINARY_DATA;
import static ru.danny.logging.utils.LoggingConstants.NO_BODY;

@Slf4j
@RequiredArgsConstructor
public class HttpLogger extends OncePerRequestFilter {

	private static final String AUTH_DATA_HEADER = "authorization-authdata";
	private static final String NO_AUTH_DATA = "[no auth data]";

	private final Pattern loggingRequestPattern;
	private final Set<String> excludeHeaders;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		long startTime = System.currentTimeMillis();

		if (shouldLog(request)) {
			var wrappedResponse = new ContentCachingResponseWrapper(response);
			var wrappedRequest = new CachedBodyHttpServletRequest(request);

			var requestBody = getBody(wrappedRequest);

			MDCUtil.putRequestDataToMDC(request);
			log.info("==> Received request {} {}{},\nheaders = [{}],\nbody = {}",
				wrappedRequest.getMethod(),
				getRequestUrl(wrappedRequest),
				Optional.ofNullable(request.getHeader(AUTH_DATA_HEADER))
					.map(header -> URLDecoder.decode(header, StandardCharsets.UTF_8))
					.orElse(NO_AUTH_DATA),
				HttpUtils.getHeadersString(wrappedRequest, excludeHeaders),
				requestBody);

			filterChain.doFilter(wrappedRequest, wrappedResponse);

			var responseTime = System.currentTimeMillis() - startTime;
			MDCUtil.putResponseDataToMDC(wrappedResponse, responseTime);
			log.info("<== Request {} {} returned status = {} in {}ms,\nheaders = [{}],\nbody = {}",
				wrappedRequest.getMethod(),
				getRequestUrl(wrappedRequest),
				wrappedResponse.getStatus(),
				responseTime,
				HttpUtils.getHeadersString(wrappedResponse, excludeHeaders),
				getBody(wrappedResponse));

			MDCUtil.clearMDC();
			wrappedResponse.copyBodyToResponse();
		} else {
			filterChain.doFilter(request, response);
		}
	}

	private boolean shouldLog(HttpServletRequest request) {
		return loggingRequestPattern.matcher(request.getRequestURI()).matches();
	}

	private String getRequestUrl(HttpServletRequest request) {
		var url = new StringBuilder(request.getRequestURI());
		String queryString = request.getQueryString();
		if (queryString != null) {
			url.append('?').append(queryString);
		}
		return url.toString();
	}

	private String getBody(HttpServletRequest request) throws IOException {
		StringBuilder body = new StringBuilder();
		String line;
		BufferedReader reader = request.getReader();
		while ((line = reader.readLine()) != null) {
			body.append(line.trim());
		}
		return !body.isEmpty()
			? body.toString()
			: NO_BODY;
	}

	private String getBody(ContentCachingResponseWrapper response) {
		var buffer = response.getContentAsByteArray();
		if (buffer.length > 0) {
			try {
				return new String(buffer, StandardCharsets.UTF_8);
			} catch (Exception e) {
				return BINARY_DATA;
			}
		}
		return NO_BODY;
	}

}
