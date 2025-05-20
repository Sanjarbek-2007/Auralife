package uz.project.auralife.controllers.auth.signup;

public record ConfirmResetPasswordDTO(
        String email, String code, String password
) {
}
