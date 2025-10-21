package uz.project.auralife.controllers.auth.signup;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.project.auralife.responces.JwtResponse;
import uz.project.auralife.responces.Response;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SignUpResponseDto extends Response {
    private JwtResponse jwtResponse;
    private  String iotDeviceId;
    public SignUpResponseDto(int status, JwtResponse jwtResponse, String iotDeviceId, String message) {
        super(status, message);
        this.jwtResponse = jwtResponse;
        this.iotDeviceId = iotDeviceId;
    }



}
