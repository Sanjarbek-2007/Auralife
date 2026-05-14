package uz.project.auralife.controllers.auth.signin;

public record GoogleTrustDto(
    String email,
    String firstName,
    String lastName,
    String deviceName,
    String deviceType,
    String app
) {}
