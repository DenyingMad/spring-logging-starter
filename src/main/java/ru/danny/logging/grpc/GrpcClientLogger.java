package ru.danny.logging.grpc;

import com.google.protobuf.InvalidProtocolBufferException;
import io.grpc.*;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.danny.logging.utils.GrpcUtils;

import java.util.Optional;

import static ru.danny.logging.utils.LoggingConstants.SERIALIZATION_ERROR;
import static ru.danny.logging.utils.LoggingConstants.UNKNOWN_STATUS;

@Slf4j
@RequiredArgsConstructor
@AllArgsConstructor
public class GrpcClientLogger implements ClientInterceptor {

	private int messageMaxLength;

	@Override
	public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> methodDescriptor, CallOptions callOptions, Channel channel) {

		return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(channel.newCall(methodDescriptor, callOptions)) {

			final long startTime = System.currentTimeMillis();

			@Override
			public void sendMessage(ReqT message) {
				try {
					String substring = GrpcUtils.getSubstring(methodDescriptor);
					Optional<String> serviceName = GrpcUtils.getServiceName(substring);

					log.info("[{}] ==> Outgoing gRPC request {}, \nbody = {}",
						serviceName.orElse(null),
						substring,
						GrpcUtils.getBody(message, messageMaxLength));
				} catch (InvalidProtocolBufferException e) {
					log.warn(SERIALIZATION_ERROR);
				}
				super.sendMessage(message);
			}

			@Override
			public void start(Listener<RespT> responseListener, Metadata headers) {
				super.start(new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(responseListener) {

					Status status;

					@Override
					public void onClose(Status status, Metadata trailers) {
						long elapsedTime = System.currentTimeMillis() - startTime;
						this.status = status;
						String substring = GrpcUtils.getSubstring(methodDescriptor);
						Optional<String> serviceName = GrpcUtils.getServiceName(substring);
						if (status.getCause() != null) {
							log.warn("[{}] <== Received gRPC response {} with {} in {}ms",
								serviceName.orElse(null),
								substring,
								status.getCode(),
								elapsedTime);
						} else if (!status.isOk()) {
							log.error("[{}] <== Received gRPC response {} with {} in {}ms",
								serviceName.orElse(null),
								substring,
								status.getCode(),
								elapsedTime);
						}
						super.onClose(status, trailers);
					}

					@Override
					public void onMessage(RespT message) {
						long elapsedTime = System.currentTimeMillis() - startTime;
						try {
							String checkedStatus = GrpcUtils.validateStatus(status);
							String substring = GrpcUtils.getSubstring(methodDescriptor);
							Optional<String> serviceName = GrpcUtils.getServiceName(substring);
							log.info("[{}] <== Received gRPC response {} with {} in {}ms,\nheaders = [{}],\nbody = {}",
								serviceName.orElse(null),
								substring,
								checkedStatus,
								elapsedTime,
								GrpcUtils.getHeaders(headers),
								GrpcUtils.getBody(message, messageMaxLength));
						} catch (InvalidProtocolBufferException e) {
							log.warn(SERIALIZATION_ERROR);
						} catch (NullPointerException e) {
							log.warn(UNKNOWN_STATUS);
						}
						super.onMessage(message);
					}
				}, headers);
			}
		};
	}

}
