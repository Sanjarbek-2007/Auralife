package uz.project.auralife.controllers.auth.signup;

import java.time.LocalDateTime;
import java.util.Date;

public record SignupDto(
        String firstName, String lastName, String username, String email, String password, String phoneNumber, Date birthDate, String gender, String app,
        String deviceName, String deviceType

) {}
