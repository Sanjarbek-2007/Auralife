package uz.project.auralife.config.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import uz.project.auralife.domains.Device;
import uz.project.auralife.domains.User;
import uz.project.auralife.domains.enums.Apps;
import uz.project.auralife.repositories.UserRepository;
import uz.project.auralife.services.AuthService;
import uz.project.auralife.services.ConnectedAppService;

import java.io.IOException;

/**
 * Called by Spring Security after a successful Google OAuth2 login.
 *
 * Flow:
 *  1. Load (or create) the Auralife user from the Google email.
 *  2. Register/find a "WEB_BROWSER" device for this user.
 *  3. Generate a JWT containing the iot_device_id claim.
 *  4. Register AURALIFE as a connected app (idempotent).
 *  5. Redirect to the Account Centre with the JWT as a URL query param.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final ConnectedAppService connectedAppService;

    @Value("${account-centre.url:http://localhost:5173}")
    private String accountCentreUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getEmail();

        log.info("Google OAuth2 success for email: {}", email);

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            log.error("User not found after OAuth2 for email: {}", email);
            response.sendError(401, "User not found after OAuth2");
            return;
        }

        // Register (or find existing) a WEB device for this Google login
        Device device = authService.getOrRegisterDevice(
                "Web Browser",
                "DESKTOP",
                user.getId(),
                Apps.AURALIFE.getValue(),
                false,
                request
        );

        // Generate JWT with the correct device id and app string
        String token = jwtProvider.generate(user, device.getIotDeviceId(), Apps.AURALIFE.getValue());

        // Track AURALIFE as a connected app (idempotent)
        connectedAppService.registerApp(user.getId(), Apps.AURALIFE);

        // Determine where to redirect: prefer session-stored redirect_uri, else account centre
        String targetUrl = (String) request.getSession().getAttribute("redirect_uri");
        String appIdStr = (String) request.getSession().getAttribute("app_id");
        
        request.getSession().removeAttribute("redirect_uri");
        request.getSession().removeAttribute("app_id");

        if (appIdStr != null && !appIdStr.isEmpty()) {
            try {
                Apps requestedApp = Apps.valueOf(appIdStr);
                if (requestedApp.getRedirectUri() != null && !requestedApp.getRedirectUri().isEmpty()) {
                    targetUrl = requestedApp.getRedirectUri();
                }
            } catch (Exception e) {
                log.warn("Invalid app_id provided during Google OAuth2 redirect: {}", appIdStr);
            }
        }

        if (targetUrl != null && !targetUrl.isEmpty()) {
            log.info("Redirecting Google OAuth to custom redirect_uri: {}", targetUrl);
            getRedirectStrategy().sendRedirect(request, response, targetUrl + "?token=" + token);
        } else {
            // Default: redirect to the Account Centre frontend
            String redirectUrl = accountCentreUrl + "?token=" + token;
            log.info("Redirecting Google OAuth to Account Centre: {}", redirectUrl);
            getRedirectStrategy().sendRedirect(request, response, redirectUrl);
        }
    }
}
