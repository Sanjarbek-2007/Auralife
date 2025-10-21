package uz.project.auralife.responces;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

@Getter
@Setter

public class SignInResponse extends Response{
    private String accessToken;
    private LocalDateTime dateTime=LocalDateTime.now();
    private String iotDeviceId;

    public SignInResponse(int status, String message, String accessToken, String iotDeviceId) {
        super(status, message);
        this.accessToken = accessToken;
        this.iotDeviceId = iotDeviceId;

    }
}
