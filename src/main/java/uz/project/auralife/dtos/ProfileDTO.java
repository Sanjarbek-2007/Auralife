package uz.project.auralife.dtos;

import uz.project.auralife.domains.Device;

import uz.project.auralife.domains.Role;

import java.util.Date;
import java.util.List;


public record ProfileDTO(
        Long id,
        String firstName,
        String lastName,
        String username,
        String email,
        String phoneNumber,
        Date birthDate,
        String gender,
        String status,
        List<Role> roles,
        String apps,
        String profilePhotoFileId,
        List<Device> devices,
        String jobTitle,
        String officeLocation
) {
}
