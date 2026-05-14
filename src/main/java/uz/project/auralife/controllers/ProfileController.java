package uz.project.auralife.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uz.project.auralife.config.UserContext;
import uz.project.auralife.domains.User;
import uz.project.auralife.dtos.ProfileDTO;
import uz.project.auralife.dtos.UpdateProfileRequest;
import uz.project.auralife.exceptions.FileUploadFailedException;
import uz.project.auralife.services.AuthService;
import uz.project.auralife.services.ProfileService;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Date;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/profile")
public class ProfileController {
    private final AuthService authService;
    private final ProfileService profileService;
    private final UserContext userContext;


    @GetMapping("/get")
    public ResponseEntity<ProfileDTO> getProfile() {

        return profileService.getProfile();
    }


    @PostMapping("/editFullname")
    public ResponseEntity<User> editFullname(@RequestParam("firstName") String firstName, @RequestParam("lastName") String lastName) {
        return profileService.editFullname(firstName, lastName);
    }

    @PostMapping("/editPassword")
    public ResponseEntity<String> editPassword(@RequestParam("password") String password) {
        return profileService.editPassword(password);
    }

    @PostMapping("/editEmail")
    public ResponseEntity<User> editEmail(@RequestParam("email") String email) {
        return profileService.editEmail(email);
    }
    @PostMapping("/editUsername")
    public ResponseEntity<User> editUsername(@RequestParam("username") String username) {
        return profileService.editUsername(username);
    }
    @PostMapping("/editBirthdate")
    public ResponseEntity<?> editBirthdate(@RequestParam("birthdate") Date birthdate) {
        return profileService.editBirthdate(birthdate);
    }

    @PostMapping("/editPhoneNumber")
    public ResponseEntity<User> editPhoneNumber(@RequestParam("phoneNumber") String phoneNumber) {
        return profileService.editPhoneNumber(phoneNumber);
    }

    @PostMapping("/uploadProfilePicture")
    public ResponseEntity<?> uploadProfilePicture(
            @RequestParam("file") MultipartFile file) throws FileUploadFailedException {
        return profileService.uploadProfilePicture(file);
    }

    @GetMapping("/get-by-id")
    public ResponseEntity<ProfileDTO> getProfileById(@RequestParam Long id) {
        return profileService.getUserById(id);
    }

    @GetMapping("/get-profile-by-username")
    public ResponseEntity<?> getProfileByUsername(@RequestParam String username) {
        return ResponseEntity.ok(profileService.getProfileByUsername(username));
    }
    @GetMapping("/search-profiles-by-username")
    public ResponseEntity<?> searchProfilesByUsername(@RequestParam String username) {
        return ResponseEntity.ok(profileService.searchProfilesByUsername(username));
    }
}
