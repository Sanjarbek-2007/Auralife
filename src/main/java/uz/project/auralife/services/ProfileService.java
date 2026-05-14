package uz.project.auralife.services;

import jakarta.transaction.Transactional;

import java.io.IOException;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uz.project.auralife.config.UserContext;
import uz.project.auralife.domains.User;
import uz.project.auralife.dtos.ProfileDTO;
import uz.project.auralife.dtos.PublicProfileDto;
import uz.project.auralife.exceptions.FileUploadFailedException;
import uz.project.auralife.repositories.RoleRepository;
import uz.project.auralife.repositories.UserRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserContext userContext;
    private final AuthService authService;
    private final FilesGrpcClient filesGrpcClient;

    @Value("${api.url}")
    private String apiUrl;

    @Transactional
    public ResponseEntity<User> editFullname(String firstName, String lastName) {
        String email = userContext.getUser().getEmail();
        userRepository.updateFirstNameByEmail(firstName, email);
        userRepository.updateLastNameByEmail(lastName, email);
        return ResponseEntity.ok(userRepository.findByEmail(email).get());
    }

    public ResponseEntity<String> editPassword(String password) {
        String email = userContext.getUser().getEmail();
        if (!email.isBlank()) {
            String encodedPassword = passwordEncoder.encode(password);
            userRepository.updatePasswordByEmail(encodedPassword, email);
            return new ResponseEntity<>("Password edited successfully", HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    @Transactional
    public ResponseEntity<User> editEmail(String email) {
        String email1 = userContext.getUser().getEmail();
        if (!email1.isBlank()) {
            userRepository.updateEmailByEmail(email1, email);
            User updatedUser = userRepository.findByPhoneNumber(email1).orElse(null);
            return new ResponseEntity<>(updatedUser, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
    public ResponseEntity<User> editUsername(String username) {
        String email = userContext.getUser().getEmail();
        if (!email.isBlank()) {
            userRepository.updateUsernameByEmail(username, email);
        }
        return null;
    }
    @Transactional
    public ResponseEntity<User> editPhoneNumber(String phoneNumber) {
        String email = userContext.getUser().getEmail();
        if (!email.isBlank()) {
            userRepository.updatePhoneNumberByEmail(email, phoneNumber);
            User updatedUser = userRepository.findByPhoneNumber(phoneNumber).orElse(null);
            return new ResponseEntity<>(updatedUser, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    public ResponseEntity<ProfileDTO> getProfile() {
        User user = userContext.getUser();

        log.info("Get profile for email {}", user.getEmail());
        if (!user.getEmail().isBlank()) {
            if (user.getStatus().equals("active")) {
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
                        user.getProfilePhotoFileId(),
                        Collections.emptyList(),
                        user.getJobTitle(),
                        user.getOfficeLocation()
                        );
                return ResponseEntity.ok(dto);
            }
        }
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    public ResponseEntity<?> uploadProfilePicture(MultipartFile file)
            throws FileUploadFailedException {

        User user = userContext.getUser();

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }

        try {
            // Delete old photo if exists
            if (user.getProfilePhotoFileId() != null && !user.getProfilePhotoFileId().isEmpty()) {
                filesGrpcClient.deleteFile(user.getProfilePhotoFileId());
            }

            String fileId = filesGrpcClient.uploadFile(file, "User:" + user.getId(), "PROFILE_PHOTO");
            user.setProfilePhotoFileId(fileId);
            userRepository.save(user);
            return ResponseEntity.ok(fileId);
        } catch (IOException e) {
            log.error("Failed to upload profile picture via gRPC", e);
            throw new FileUploadFailedException("Could not upload profile picture");
        }
    }


    public ResponseEntity<ProfileDTO> getUserById(Long id) {

        User user = userRepository.findById(id).get();
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
                user.getProfilePhotoFileId(),
                Collections.emptyList(),
                user.getJobTitle(),
                user.getOfficeLocation()
                );
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    public User getUserByToken(String token) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).get();
    }

    public List<ProfileDTO> mapToProfileDTO(List<User> profiles) {
        List<ProfileDTO> profileDTOs = new ArrayList<>();
        for (User user : profiles) {
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
                    user.getProfilePhotoFileId(),
                    Collections.emptyList(),
                    user.getJobTitle(),
                    user.getOfficeLocation()
            );
            profileDTOs.add(dto);
        }
        return profileDTOs;
    }

    public ResponseEntity<List<ProfileDTO>> getAllUsers() {
        return new ResponseEntity<>(mapToProfileDTO(userRepository.findAll()), HttpStatus.OK);
    }

    public ResponseEntity<?> deactivateUser(Long userId) {
        userRepository.updateStatusById("non-active", userId);
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<?> activateUser(Long userId) {
        userRepository.updateStatusById("active", userId);
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<?> deleteUser(Long userId) {
        userRepository.deleteById(userId);
        return ResponseEntity.ok().build();
    }


    public ResponseEntity<?> editBirthdate(Date birthdate) {

        userRepository.updateBirthDateByEmail(birthdate, userContext.getUser().getEmail());
        return ResponseEntity.ok().build();
    }

    public PublicProfileDto getProfileByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(user -> new PublicProfileDto(
                        user.getId(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getUsername(),
                        user.getGender(),
                        user.getProfilePhotoFileId()
                ))
                .orElse(null); // or throw exception if not found
    }

    public List<PublicProfileDto> searchProfilesByUsername(String username) {
        return userRepository.findByUsernameContainingIgnoreCase(username)
                .stream()
                .map(user -> new PublicProfileDto(
                        user.getId(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getUsername(),
                        user.getGender(),
                        user.getProfilePhotoFileId()
                ))
                .toList();
    }

    @Transactional
    public ResponseEntity<User> updateProfile(uz.project.auralife.dtos.UpdateProfileRequest request) {
        User user = userContext.getUser();
        if (request.firstName() != null) user.setFirstName(request.firstName());
        if (request.lastName() != null) user.setLastName(request.lastName());
        if (request.phoneNumber() != null) user.setPhoneNumber(request.phoneNumber());
        if (request.jobTitle() != null) user.setJobTitle(request.jobTitle());
        if (request.officeLocation() != null) user.setOfficeLocation(request.officeLocation());

        userRepository.save(user);
        return ResponseEntity.ok(user);
    }

}
