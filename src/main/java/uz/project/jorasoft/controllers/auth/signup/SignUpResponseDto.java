package uz.project.jorasoft.controllers.auth.signup;

import uz.project.jorasoft.responces.JwtResponse;
import uz.project.jorasoft.responces.Response;

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
