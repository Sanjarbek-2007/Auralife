package uz.project.auralife.config.security;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@EnableWebSecurity
@EnableMethodSecurity(jsr250Enabled = true, securedEnabled = true)
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Auralife",
                description = "This application is for monkey only",
                version = "${app.version}",
                contact = @Contact(
                        name = "Auralife",
                        url = "https://github.com/Sanjarbek-2007/Auralife",
                        email = "olix@gmail.com"
                ),
                license = @License(
                        name = "Apache 3.0",
                        url = "https://starter.spring.io"
                ),
                termsOfService = "https://wwww.wiki.com",
                summary = "The project is fully supported by DS team"
        ),
        servers = {
                @Server(url = "http://localhost:20007/", description = "Development Server"),
                @Server(url = "https://*.ngrok-free.app", description = "Ngrok Public Server")
        }
)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity security) throws Exception {
        security
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(CsrfConfigurer::disable)
                .authorizeHttpRequests(registr ->
                        registr
                                .requestMatchers("/auth/**", "/try/**", "/v3/**", "/swagger-ui/**","/api/**", "/photo/**").permitAll()
                                .requestMatchers("/error").permitAll() // Allow error endpoint
                                .anyRequest().authenticated()
                )
                .sessionManagement(config -> config.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return security.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOriginPattern("https://*.ngrok-free.app");
        configuration.addAllowedOrigin("http://localhost:20007");
        configuration.addAllowedOrigin("http://localhost:11911");
        configuration.addAllowedOrigin("http://26.42.249.167:3000");
        configuration.addAllowedOrigin("http://localhost:3000");
        configuration.addAllowedOrigin("http://*.trycloudflare.com"); // Add if testing with other local ports
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList("jwt auth"))
                .components(new Components().addSecuritySchemes("jwt auth", new SecurityScheme()
                        .name("jwt auth")
                        .type(SecurityScheme.Type.HTTP)
                        .bearerFormat("JWT")
                        .in(SecurityScheme.In.HEADER)
                        .scheme("bearer")));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}