package uz.project.auralife.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.project.auralife.domains.User;
import uz.project.auralife.repositories.UserRepository;

@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
@RequiredArgsConstructor
public class UserContext {
    private User user;


    private String token;
    private String iotDeviceId;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public User getUser() {
        if (user == null) {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            System.out.println("UserContext: Fetching user for email: " + email);
            if (email != null && !email.equals("anonymousUser")) {
                user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new IllegalArgumentException("User not found for email: " + email));
                System.out.println("UserContext: User fetched: " + user.getId());
            } else {
                System.out.println("UserContext: No authenticated user found");
                throw new IllegalStateException("No authenticated user found");
            }
        }
        return user;
    }

    public void setIotDeviceId(String iotDeviceId) {
        this.iotDeviceId = iotDeviceId;
    }

    public String getIotDeviceId() {
        return iotDeviceId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setUser(User user) {
        this.user = user;
    }
}