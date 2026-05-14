package uz.project.auralife.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import uz.project.auralife.domains.Role;
import uz.project.auralife.domains.User;
import uz.project.auralife.repositories.RoleRepository;
import uz.project.auralife.repositories.UserRepository;

import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        System.out.println("Processing Google OAuth login...");

        String email = oAuth2User.getAttribute("email");
        String firstName = oAuth2User.getAttribute("given_name");
        String lastName = oAuth2User.getAttribute("family_name");

        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            // Auto-register the new Google user with sensible defaults
            Role userRole = roleRepository.findByName("USER")
                    .orElseGet(() -> roleRepository.save(new Role("USER", "AURALIFE")));

            User user = new User();
            user.setEmail(email);
            user.setFirstName(firstName != null ? firstName : "");
            user.setLastName(lastName != null ? lastName : "");
            user.setUsername(email.split("@")[0]);
            user.setStatus("active");
            // Set default apps to avoid NPE in ProfileService / AuthService
            user.setApps("AURALIFE");
            user.setRoles(Collections.singletonList(userRole));
            userRepository.save(user);
        }

        return new CustomOAuth2User(oAuth2User);
    }
}
