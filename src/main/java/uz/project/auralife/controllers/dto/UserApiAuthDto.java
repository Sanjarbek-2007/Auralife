package uz.project.auralife.controllers.dto;

public record UserApiAuthDto(String secretKey, Long userId, String password) {
}
