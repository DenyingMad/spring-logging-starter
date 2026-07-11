package ru.danny.logging.grpc;

import com.google.protobuf.InvalidProtocolBufferException;
import io.grpc.*;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.danny.logging.utils.GrpcUtils;

import static ru.danny.logging.utils.LoggingConstants.SERIALIZATION_ERROR;
import static ru.danny.logging.utils.LoggingConstants.UNKNOWN_STATUS;

@Slf4j
@RequiredArgsConstructor
@AllArgsConstructor
public class GrpcServerLogger implements ServerInterceptor {

	private int messageMaxLength;

	@Override
	public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall,
		Metadata metadata,
		ServerCallHandler<ReqT, RespT> serverCallHandler) {
		GrpcServerCall<ReqT, RespT> grpcServerCall = new GrpcServerCall<>(serverCall, messageMaxLength);

		return new ForwardingServerCallListener.SimpleForwardingServerCallListener<>(serverCallHandler.startCall(grpcServerCall, metadata)) {


			@Override
			public void onMessage(ReqT request) {

				try {
					log.info("==> Received gRPC request {}, \nheaders = [{}], \nbody = {}",
						GrpcUtils.getSubstring(grpcServerCall.getMethodDescriptor()),
						GrpcUtils.getHeaders(metadata),
						GrpcUtils.getBody(request, messageMaxLength));
				} catch (InvalidProtocolBufferException e) {
					log.warn(SERIALIZATION_ERROR);
				}

				super.onMessage(request);
			}

		};
	}

	private static class GrpcServerCall<ReqT, RespT> extends ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT> {

		final long startTime = System.currentTimeMillis();
		private final int messageMaxLength;
		Metadata metadata;
		Status status;

		protected GrpcServerCall(ServerCall<ReqT, RespT> serverCall, int messageMaxLength) {
			super(serverCall);
			this.messageMaxLength = messageMaxLength;
		}

		@Override
		public void close(Status status, Metadata trailers) {
			long elapsedTime = System.currentTimeMillis() - startTime;
			this.status = status;
			this.metadata = trailers;
			if (status.getCause() != null) {
				log.warn("<== Sent gRPC response {} with {} in {}ms",
					GrpcUtils.getSubstring(super.getMethodDescriptor()),
					status.getCode(),
					elapsedTime);
			} else if (!status.isOk()) {
				log.error("<== Sent gRPC response {} with {} in {}ms",
					GrpcUtils.getSubstring(super.getMethodDescriptor()),
					status.getCode(),
					elapsedTime);
			}
			super.close(status, trailers);
		}

		@Override
		public void sendMessage(RespT message) {
			long elapsedTime = System.currentTimeMillis() - startTime;
			try {
				String checkedStatus = GrpcUtils.validateStatus(status);
				log.info("<== Sent gRPC response {} with status {} in {}ms, \nheaders = [{}], \nbody = {}",
					GrpcUtils.getSubstring(super.getMethodDescriptor()),
					checkedStatus,
					elapsedTime,
					GrpcUtils.getHeaders(metadata),
					GrpcUtils.getBody(message, messageMaxLength));
			} catch (InvalidProtocolBufferException e) {
				log.warn(SERIALIZATION_ERROR);
			} catch (NullPointerException e) {
				log.warn(UNKNOWN_STATUS);
			}
			super.sendMessage(message);
		}

	}

}
