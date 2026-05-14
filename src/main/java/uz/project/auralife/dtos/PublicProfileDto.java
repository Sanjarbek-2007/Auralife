package uz.project.auralife.dtos;

import uz.project.auralife.domains.Device;


import java.util.List;

public record PublicProfileDto(Long id, String firstName, String lastname, String username, String gender, String profilePhotoFileId) {}
