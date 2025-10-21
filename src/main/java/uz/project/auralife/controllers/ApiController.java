package uz.project.auralife.controllers;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import uz.project.auralife.config.security.JwtProvider;
import uz.project.auralife.domains.Device;
import uz.project.auralife.domains.User;
import uz.project.auralife.dtos.ProfileDTO;
import uz.project.auralife.repositories.DeviceRepository;
import uz.project.auralife.repositories.UserRepository;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ApiController {
    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;
    @Value("${secret.key}")
    private String secretKey;
    // Inject your JwtProvider or token validation service here
    private final JwtProvider jwtProvider;

    public ApiController(UserRepository userRepository, JwtProvider jwtProvider, DeviceRepository deviceRepository) {
        this.userRepository = userRepository;
        this.jwtProvider = jwtProvider;
        this.deviceRepository = deviceRepository;
    }

    @GetMapping("/validate")
    public Boolean validateToken(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid Authorization header");
            return false;
        }

        String token = authHeader.substring(7);

        // TODO: Replace this with your real validation logic

        return jwtProvider.validate(token);

    }

    @GetMapping("/get-user-by-id")
    public ResponseEntity<ProfileDTO> getProfileById(@RequestBody GetUserByIdDto dto) {

        if (!secretKey.equals(dto.secret())) return ResponseEntity.badRequest().build();

        try {
            User user = userRepository.findById(dto.id()).orElse(null);
            List<Device> devices = deviceRepository.findByUserId(dto.id());
            if (user == null) {
                System.out.println("User not found for ID " + dto.id());
                return ResponseEntity.notFound().build();
            }

            ProfileDTO profileDTO = new ProfileDTO(
                    user.getId(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getPhoneNumber(),
                    user.getBirthDate(),
                    user.getGender(),
                    user.getStatus(),
                    user.getRoles(),
                    user.getApps(),
                    user.getProfilePictures(),devices);

            System.out.println("User by id received: " + profileDTO.email());
            return new ResponseEntity<>(profileDTO, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
