package uz.project.auralife.controllers.auth.signup;

import uz.project.auralife.responces.JwtResponse;
import uz.project.auralife.responces.Response;

public class SignUpResponseDto extends Response {
    private JwtResponse jwtResponse;

    public SignUpResponseDto(int status, JwtResponse jwtResponse, String message) {
        super(status, message);
        this.jwtResponse = jwtResponse;
    }

    public SignUpResponseDto(JwtResponse jwtResponse, String message) {
        this.jwtResponse = jwtResponse;
        super.setMessage(message);
    }
}
