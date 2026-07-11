package ru.danny.logging.feign;

import feign.Logger;
import feign.Request;
import feign.Response;
import feign.Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.danny.logging.utils.FeignUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Set;

import static feign.Util.UTF_8;
import static feign.Util.decodeOrDefault;
import static ru.danny.logging.utils.LoggingConstants.NO_BODY;

@Slf4j
@RequiredArgsConstructor
public class FeignLogger extends Logger {

	private final Set<String> excludeHeaders;

	@Override
	protected void log(String s, String s1, Object... objects) {
		log.info(String.format(methodTag(s) + s1, objects));
	}

	@Override
	protected void logRequest(String configKey, Level logLevel, Request request) {
		String body = request.body() != null ? new String(request.body(), request.charset()) : NO_BODY;
		log(configKey, "==> Outgoing request %s %s,\nheaders = [%s],\nbody = %s",
			request.httpMethod().name(),
			request.url(),
			FeignUtils.getHeadersString(request.headers(), excludeHeaders),
			body);
	}

	@Override
	protected void logRetry(String configKey, Level logLevel) {
		log(configKey, "==> Retrying");
	}

	@Override
	protected Response logAndRebufferResponse(String configKey, Level logLevel, Response response, long elapsedTime) throws IOException {
		int status = response.status();
		if (response.body() != null && !(status == 204 || status == 205)) {
			byte[] bodyData = Util.toByteArray(response.body().asInputStream());
			log(configKey, "<== Request %s %s returned status %d in %dms,\nheaders = [%s],\nbody = %s",
				response.request().httpMethod().name(),
				response.request().url(),
				response.status(),
				elapsedTime,
				FeignUtils.getHeadersString(response.headers(), excludeHeaders),
				decodeOrDefault(bodyData, UTF_8, NO_BODY));
			return response.toBuilder().body(bodyData).build();
		} else {
			log(configKey, "<== Request %s %s returned status %d in %dms,\nheaders = [%s]",
				response.request().httpMethod().name(),
				response.request().url(),
				response.status(),
				elapsedTime,
				FeignUtils.getHeadersString(response.headers(), excludeHeaders));
		}
		return response;
	}

	@Override
	protected IOException logIOException(String configKey, Level logLevel, IOException ioe, long elapsedTime) {
		StringWriter sw = new StringWriter();
		ioe.printStackTrace(new PrintWriter(sw));
		log(configKey, "<== Request ERROR %s: %s after %dms, stacktrace = \n%s",
			ioe.getClass().getSimpleName(),
			ioe.getMessage(),
			elapsedTime,
			sw);
		return ioe;
	}

}
