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
    private String firstName;
    private String lastName;
    private String profilePhotoFileId;
    private String redirectUri;

    public SignInResponse(int status, String message, String accessToken, String iotDeviceId) {
        super(status, message);
        this.accessToken = accessToken;
        this.iotDeviceId = iotDeviceId;
    }

    public SignInResponse(int status, String message, String accessToken, String iotDeviceId, String firstName, String lastName, String profilePhotoFileId) {
        super(status, message);
        this.accessToken = accessToken;
        this.iotDeviceId = iotDeviceId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.profilePhotoFileId = profilePhotoFileId;
    }
}
