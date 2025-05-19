package uz.project.jorasoft.controllers.auth.signup;

public record ConfirmResetPasswordDTO(
        String email, String code, String password
) {
}
