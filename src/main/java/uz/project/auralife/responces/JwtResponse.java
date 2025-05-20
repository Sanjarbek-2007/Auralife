package uz.project.auralife.responces;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JwtResponse extends Response {
    private String accessToken;
    private LocalDateTime dateTime=LocalDateTime.now();

    public JwtResponse(String accessToken) {
        this.accessToken = accessToken;
    }
    public JwtResponse(String accessToken, String message) {
        this.accessToken = accessToken;
        super.setMessage(message);
    }
}
