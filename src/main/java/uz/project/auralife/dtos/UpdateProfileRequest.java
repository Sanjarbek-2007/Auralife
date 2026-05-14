package uz.project.auralife.dtos;

public record UpdateProfileRequest(
    String firstName,
    String lastName,
    String phoneNumber,
    String jobTitle,
    String officeLocation
) {}
