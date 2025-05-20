package uz.project.auralife.controllers.auth.signup;

public record ActivateRequestDTO (
        String email,
        String code,
        String date
){
}
