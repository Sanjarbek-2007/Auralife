package uz.project.auralife.config.security;

import net.devh.boot.grpc.server.security.authentication.BearerAuthenticationReader;
import net.devh.boot.grpc.server.security.authentication.GrpcAuthenticationReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcSecurityConfig {

    @Bean
    public GrpcAuthenticationReader grpcAuthenticationReader() {
        // This allows us to use Bearer tokens for gRPC calls if we want, 
        // but primarily satisfies the dependency requirement to start the server.
        return new BearerAuthenticationReader(token -> {
            // Basic validation logic could go here if needed
            return null; // For now return null to avoid complex auth for the demo
        });
    }
}
