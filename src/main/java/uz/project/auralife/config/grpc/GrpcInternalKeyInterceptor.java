package uz.project.auralife.config.grpc;

import io.grpc.*;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@GrpcGlobalServerInterceptor
public class GrpcInternalKeyInterceptor implements ServerInterceptor {

    @Value("${internal.api-key}")
    private String internalApiKey;

    private static final Metadata.Key<String> INTERNAL_KEY_HEADER =
            Metadata.Key.of("x-internal-key", Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        String clientKey = headers.get(INTERNAL_KEY_HEADER);

        if (internalApiKey == null || internalApiKey.isEmpty() || !internalApiKey.equals(clientKey)) {
            log.warn("Unauthorized gRPC call attempt. Client key: {}. Internal key: {}", clientKey, internalApiKey);
            call.close(Status.UNAUTHENTICATED.withDescription("Invalid or missing internal API key"), new Metadata());
            return new ServerCall.Listener<ReqT>() {};
        }

        return next.startCall(call, headers);
    }
}
