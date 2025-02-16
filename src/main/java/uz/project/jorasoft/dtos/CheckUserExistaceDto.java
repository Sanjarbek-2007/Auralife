package uz.project.jorasoft.dtos;

import lombok.Builder;

@Builder
public record CheckUserExistaceDto(
        String phoneNumber,
        String email
) {}
