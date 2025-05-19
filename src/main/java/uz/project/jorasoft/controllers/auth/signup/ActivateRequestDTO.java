package uz.project.jorasoft.controllers.auth.signup;

public record ActivateRequestDTO (
        String email,
        String code,
        String date
){
}
