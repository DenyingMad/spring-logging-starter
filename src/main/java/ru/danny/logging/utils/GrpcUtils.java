package ru.danny.logging.utils;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;
import lombok.experimental.UtilityClass;

import java.util.Optional;
import java.util.stream.Collectors;

import static ru.danny.logging.utils.LoggingConstants.*;

@UtilityClass
public class GrpcUtils {

	private static final JsonFormat.Printer jsonPrinter = JsonFormat.printer().omittingInsignificantWhitespace().includingDefaultValueFields();

	public static <ReqT> String getBody(ReqT message, int maxMessageLength) throws InvalidProtocolBufferException {
		String jsonMessage = jsonPrinter.print((MessageOrBuilder) message);
		return message != null
			? BodyLogUtils.truncate(jsonMessage, maxMessageLength)
			: NO_BODY;
	}

	public static String getHeaders(Metadata headers) {
		return headers != null ? GrpcUtils.formatHeaders(headers) : NO_HEADERS;
	}

	public static String formatHeaders(Metadata headers) {
		return headers.keys()
			.stream()
			.map(key ->
				String.format(HEADER_FORMAT, key,
					headers.get(Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER))))
			.collect(Collectors.joining(", "));
	}

	public <RespT, ReqT> String getSubstring(MethodDescriptor<ReqT, RespT> methodDescriptor) {
		String fullMethodName = methodDescriptor.getFullMethodName();
		int lastDotIndex = fullMethodName.lastIndexOf(".");
		return lastDotIndex != -1 ? fullMethodName.substring(lastDotIndex + 1) : fullMethodName;
	}

	public <RespT, ReqT> Optional<String> getServiceName(String shortString) {
		return Optional.of(shortString.substring(0, shortString.indexOf('/')));
	}

	public String validateStatus(Status status) {
		return status != null ? status.getCode().toString() : OK;
	}

}
