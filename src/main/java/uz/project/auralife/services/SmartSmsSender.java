package uz.project.auralife.services;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
public class SmartSmsSender {

    private final RestTemplate restTemplate = new RestTemplate();

    // Replace these with your actual credentials
    private final String username = "your_username";
    private final String password = "your_password";

    // API endpoints
    private final String authUrl = "https://notify.eskiz.uz/api/auth/login";
    private final String sendSmsUrl = "https://notify.eskiz.uz/api/message/sms/send";

    private String token;
    private Instant tokenExpiry;

    private synchronized void authenticate() {
        Map<String, String> body = new HashMap<>();
        body.put("email", username);
        body.put("password", password);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<Map> response = restTemplate.exchange(authUrl, HttpMethod.POST, request, Map.class);
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            token = (String) data.get("token");
            tokenExpiry = Instant.now().plusSeconds(3600); // 1 hour token
        } catch (Exception e) {
            throw new RuntimeException("Authentication failed: " + e.getMessage(), e);
        }
    }

    private synchronized void ensureTokenValid() {
        if (token == null || Instant.now().isAfter(tokenExpiry)) {
            authenticate();
        }
    }

    public boolean sendSms(String phoneNumber, String message) {
        ensureTokenValid();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        Map<String, String> body = new HashMap<>();
        body.put("mobile_phone", phoneNumber);
        body.put("message", message);
        body.put("from", "4546");

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(sendSmsUrl, request, Map.class);
            int statusCode = response.getStatusCodeValue();
            return statusCode == 200;
        } catch (Exception e) {
            // Retry if token expired and it wasn't caught earlier
            if (e.getMessage().contains("401")) {
                token = null;
                return sendSms(phoneNumber, message); // recursive retry
            }
            throw new RuntimeException("Failed to send SMS: " + e.getMessage(), e);
        }
    }
}

