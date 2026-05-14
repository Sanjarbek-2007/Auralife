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

import java.util.List;

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
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity security) throws Exception {
        security
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(CsrfConfigurer::disable)
                .authorizeHttpRequests(registr ->
                        registr
                                // Public endpoints: auth pages, swagger, static assets, OAuth2, error
                                .requestMatchers(
                                        "/auth/**",
                                        "/try/**",
                                        "/v3/**",
                                        "/swagger-ui/**",
                                        "/photo/**",
                                        "/files/**",
                                        "/assets/**",
                                        "/css/**",
                                        "/js/**",
                                        "/oauth2/**",
                                        "/login/oauth2/**",
                                        "/error",
                                        "/api/v1/billing/plans",         // Public plan listing
                                        "/api/v1/billing/click/prepare",  // Click SHOP-API webhook
                                        "/api/v1/billing/click/complete"  // Click SHOP-API webhook
                                ).permitAll()
                                .requestMatchers("/api/v1/admin/**").hasAnyAuthority("ROLE_ADMIN", "ADMIN")
                                // Everything else requires authentication
                                .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/auth/page/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/auth/page/login?error=true")
                        .permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/auth/page/login")
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                )
                .sessionManagement(config -> config.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return security.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:5173",
                "http://localhost:3000",
                "http://localhost:*",
                "https://*.ngrok-free.app",
                "http://system-controll-panel.auralife.uz",
                "https://system-controll-panel.auralife.uz",
                "http://*.auralife.uz",
                "https://*.auralife.uz"
        ));
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
}