package uz.project.auralife.grpc;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import lombok.RequiredArgsConstructor;
import uz.project.auralife.config.security.JwtProvider;
import uz.project.auralife.domains.User;
import uz.project.auralife.grpc.proto.AuthServiceGrpc;
import uz.project.auralife.grpc.proto.TokenRequest;
import uz.project.auralife.grpc.proto.TokenResponse;
import uz.project.auralife.grpc.proto.UserRequest;
import uz.project.auralife.grpc.proto.UserResponse;
import uz.project.auralife.repositories.UserRepository;
import uz.project.auralife.domains.Device;
import uz.project.auralife.grpc.proto.DeviceResponse;
import uz.project.auralife.repositories.DeviceRepository;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;

import java.util.stream.Collectors;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class AuthGrpcService extends AuthServiceGrpc.AuthServiceImplBase {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;

    @Override
    public void validateToken(TokenRequest request, StreamObserver<TokenResponse> responseObserver) {
        String token = request.getToken();
        TokenResponse.Builder responseBuilder = TokenResponse.newBuilder();

        try {
            Claims claims = jwtProvider.parse(token); 
            String username = claims.getSubject();
            
            responseBuilder.setIsValid(true);
            responseBuilder.setUsername(username);
            log.info("Successfully validated token via gRPC for user: {}", username);
        } catch (Exception e) {
            log.error("Failed to validate token via gRPC", e);
            responseBuilder.setIsValid(false);
            responseBuilder.setErrorMessage(e.getMessage() != null ? e.getMessage() : "Invalid token");
        }

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getUser(UserRequest request, StreamObserver<UserResponse> responseObserver) {
        String token = request.getToken();
        log.info("Received gRPC getUser request for token: {}", token.substring(0, Math.min(token.length(), 10)) + "...");
        
        try {
            Claims claims = jwtProvider.parse(token);
            String email = claims.getSubject();
            String iotDeviceId = claims.get("iot_device_id", String.class);
            
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found: " + email));
            
            UserResponse.Builder responseBuilder = UserResponse.newBuilder()
                    .setId(user.getId() != null ? user.getId() : 0L)
                    .setUsername(user.getUsername() != null ? user.getUsername() : "")
                    .setEmail(user.getEmail() != null ? user.getEmail() : "")
                    .setFirstName(user.getFirstName() != null ? user.getFirstName() : "")
                    .setLastName(user.getLastName() != null ? user.getLastName() : "")
                    .setPhoneNumber(user.getPhoneNumber() != null ? user.getPhoneNumber() : "")
                    .addAllRoles(user.getRoles().stream().map(role -> role.getName()).collect(Collectors.toList()));

            if (iotDeviceId != null) {
                deviceRepository.findByIotDeviceId(iotDeviceId).ifPresent(device -> {
                    DeviceResponse deviceResponse = DeviceResponse.newBuilder()
                            .setDeviceName(device.getDeviceName() != null ? device.getDeviceName() : "")
                            .setDeviceType(device.getDeviceType() != null ? device.getDeviceType() : "")
                            .setLocation(device.getLocation() != null ? device.getLocation() : "")
                            .setBrowser(device.getBrowser() != null ? device.getBrowser() : "")
                            .setIpAddress(device.getIpAddress() != null ? device.getIpAddress() : "")
                            .build();
                    responseBuilder.setDevice(deviceResponse);
                });
            }
            
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
            log.info("Successfully fetched user via gRPC: {}", email);
        } catch (Exception e) {
            log.error("Failed to fetch user via gRPC", e);
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }
}
