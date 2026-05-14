package uz.project.auralife.services;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class GeoIPService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Data
    public static class IpApiResponse {
        private String status;
        private String country;
        private String city;
        private String query; // IP address
    }

    public String getLocation(String ip) {
        if (ip == null || ip.equals("0:0:0:0:0:0:0:1") || ip.equals("127.0.0.1") || ip.equals("unknown")) {
            return "Localhost";
        }
        try {
            String url = "http://ip-api.com/json/" + ip;
            IpApiResponse response = restTemplate.getForObject(url, IpApiResponse.class);
            if (response != null && "success".equals(response.getStatus())) {
                return response.getCity() + ", " + response.getCountry();
            }
        } catch (Exception e) {
            log.error("Failed to fetch location for IP: {}", ip, e);
        }
        return "Unknown Location";
    }
}
