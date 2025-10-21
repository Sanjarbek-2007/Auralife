package uz.project.auralife.controllers.auth.signin;

public record SigninDto(
        String phoneNumber,
        String password,
        String deviceName, String deviceType, String app
) {}
