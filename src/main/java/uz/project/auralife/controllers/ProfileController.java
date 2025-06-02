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
import uz.project.auralife.domains.Photo;
import uz.project.auralife.domains.User;
import uz.project.auralife.dtos.ProfileDTO;
import uz.project.auralife.exceptions.FileUploadFailedException;
import uz.project.auralife.services.AuthService;
import uz.project.auralife.services.ProfileService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/profile")
public class ProfileController {
    private final AuthService authService;
    private final ProfileService profileService;

    @GetMapping("/get")
    public ResponseEntity<ProfileDTO> getProfile() {
        log.info("Get profile ....         .............           .......... ");
        return profileService.getProfile();
    }

    @GetMapping("/profilePhoto")
    public ResponseEntity<Photo> getProfilePhoto() {
        return profileService.getProfilePicture();
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

    @PostMapping("/editPhoneNumber")
    public ResponseEntity<User> editPhoneNumber(@RequestParam("phoneNumber") String phoneNumber) {
        return profileService.editPhoneNumber(phoneNumber);
    }

    @PostMapping("/uploadProfilePicture")
    public ResponseEntity<List<Photo>> uploadProfilePicture(@RequestParam("files") List<MultipartFile> files) throws FileUploadFailedException {
        return profileService.uploadProfilePictures(files);
    }
}
