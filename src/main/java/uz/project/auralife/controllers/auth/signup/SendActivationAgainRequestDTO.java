package uz.project.auralife.controllers.auth.signup;

public record SendActivationAgainRequestDTO(
        String email,
        String time,
        String type
) {
}
