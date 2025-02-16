package uz.project.jorasoft.controllers.auth.signup;

import java.util.Date;

public record SignupDto(
        String firstName,
        String lastName,
        String email,
        String password,
        String phoneNumber,
        Date birthDate,
        String gender

) {}
