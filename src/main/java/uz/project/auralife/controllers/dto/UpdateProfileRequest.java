package uz.project.auralife.controllers.dto;

/**
 * Request body for PUT /api/v1/account/profile
 * All fields are optional — nulls are ignored on the backend.
 */
public record UpdateProfileRequest(
        String firstName,
        String lastName,
        String phoneNumber
) {}
