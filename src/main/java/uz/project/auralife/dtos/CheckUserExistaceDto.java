package uz.project.auralife.dtos;

import lombok.Builder;

@Builder
public record CheckUserExistaceDto(
        String phoneNumber,
        String email
) {}
