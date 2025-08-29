package uz.project.auralife.controllers;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import uz.project.auralife.config.security.JwtProvider;
import uz.project.auralife.domains.User;
import uz.project.auralife.dtos.ProfileDTO;
import uz.project.auralife.repositories.UserRepository;

@RestController
@RequestMapping("/api")
public class ApiController {
    private final UserRepository userRepository;
    @Value("${secret.key}")
    private String secretKey;
    // Inject your JwtProvider or token validation service here
    private final JwtProvider jwtProvider;

    public ApiController(UserRepository userRepository, JwtProvider jwtProvider) {
        this.userRepository = userRepository;
        this.jwtProvider = jwtProvider;
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
    public ResponseEntity<ProfileDTO> getProfileById(@RequestParam Long id, @RequestParam String secret) {

        if (!secretKey.equals(secret)) return ResponseEntity.badRequest().build();

        try {
            User user = userRepository.findById(id).orElse(null);
            if (user == null) {
                System.out.println("User not found for ID " + id);
                return ResponseEntity.notFound().build();
            }

            ProfileDTO dto = new ProfileDTO(
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
                    user.getProfilePictures());

            System.out.println("User by id received: " + dto.email());
            return new ResponseEntity<>(dto, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
