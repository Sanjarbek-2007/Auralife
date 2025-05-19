package uz.project.jorasoft.controllers.auth.signup;

public record SendActivationAgainRequestDTO(
        String email,
        String time,
        String type
) {
}
