package uz.project.auralife.services;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.project.auralife.domains.User;
import uz.project.auralife.repositories.UserRepository;

@Service
@RequiredArgsConstructor
public class TwoFactorService {

    private final UserRepository userRepository;
    private final GoogleAuthenticator gAuth = new GoogleAuthenticator();

    public String generateSecret(Long userId) {
        final GoogleAuthenticatorKey key = gAuth.createCredentials();
        User user = userRepository.findById(userId).orElseThrow();
        user.setTwoFactorSecret(key.getKey());
        userRepository.save(user);
        return key.getKey();
    }

    public String getQrCodeUrl(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        GoogleAuthenticatorKey key = new GoogleAuthenticatorKey.Builder(user.getTwoFactorSecret()).build();
        return GoogleAuthenticatorQRGenerator.getOtpAuthURL("Auralife", user.getEmail(), key);
    }

    public boolean verifyCode(Long userId, int code) {
        User user = userRepository.findById(userId).orElseThrow();
        return gAuth.authorize(user.getTwoFactorSecret(), code);
    }

    public void enableTwoFactor(Long userId, int code) {
        if (verifyCode(userId, code)) {
            User user = userRepository.findById(userId).orElseThrow();
            user.setTwoFactorEnabled(true);
            userRepository.save(user);
        } else {
            throw new RuntimeException("Invalid 2FA code");
        }
    }

    public void disableTwoFactor(Long userId, int code) {
        if (verifyCode(userId, code)) {
            User user = userRepository.findById(userId).orElseThrow();
            user.setTwoFactorEnabled(false);
            user.setTwoFactorSecret(null);
            userRepository.save(user);
        } else {
            throw new RuntimeException("Invalid 2FA code");
        }
    }
}
