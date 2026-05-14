package uz.project.auralife.config.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import uz.project.auralife.config.UserContext;
import uz.project.auralife.repositories.UserRepository;
import uz.project.auralife.domains.User;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER = "Bearer ";

    private final RequestAttributeSecurityContextRepository repository
            = new RequestAttributeSecurityContextRepository();
    private final JwtProvider jwtProvider;
    private final UserContext userContext;
    private final UserRepository userRepository;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path.equals("/auth/sso/exchange") || path.equals("/auth/quit-device")) {
            return false; // MUST be filtered securely
        }
        return path.startsWith("/auth/") ||
                path.startsWith("/try/") ||
                path.startsWith("/v3/") ||
                path.startsWith("/swagger-ui/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader(AUTHORIZATION);
        if (header == null || !header.startsWith(BEARER)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(BEARER.length());

        if (!jwtProvider.validate(token)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired JWT token");
            return;
        }
        userContext.setToken(token);
        userContext.setIotDeviceId(jwtProvider.getIotDeviceId(token));
        Claims claims = jwtProvider.parse(token);
        String email = claims.getSubject();

        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            if (Boolean.TRUE.equals(user.getIsBanned())) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Your account has been banned.");
                return;
            }
            userRepository.recordActivityByEmail(email);
        }

        List<String> roles = Arrays.asList(claims.get("roles").toString().split(","));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        email,
                        null,
                        roles.stream().map(SimpleGrantedAuthority::new).toList()
                )
        );
        repository.saveContext(SecurityContextHolder.getContext(), request, response);

        filterChain.doFilter(request, response);
    }
}