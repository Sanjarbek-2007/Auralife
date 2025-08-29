package uz.project.auralife.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uz.project.auralife.config.UserContext;
import uz.project.auralife.domains.Photo;
import uz.project.auralife.domains.User;
import uz.project.auralife.dtos.ProfileDTO;
import uz.project.auralife.exceptions.FileUploadFailedException;
import uz.project.auralife.repositories.PhotoRepository;
import uz.project.auralife.repositories.RoleRepository;
import uz.project.auralife.repositories.UserRepository;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
@Slf4j
@Service
@RequiredArgsConstructor
public class   ProfileService {

    private final PhotoRepository photoRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final PhotoService photoService;
    private final UserRepository userRepository;
    private final UserContext userContext;

    @Value("${api.photo-save-path}")
    private  String path = new String();



    @Transactional
    public ResponseEntity<User> editFullname(String firstName, String lastName) {
        String email = userContext.getUser().getEmail();
        userRepository.updateFirstNameByEmail(firstName,email );
        userRepository.updateLastNameByEmail(lastName,email);
        return ResponseEntity.ok(userRepository.findByEmail(email).get());
    }

    public ResponseEntity<String> editPassword(String password) {
        String email = userContext.getUser().getEmail();
        if (!email.isBlank()) {
            String encodedPassword = passwordEncoder.encode(password);
            userRepository.updatePasswordByEmail( encodedPassword,email);
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

    public ResponseEntity<Photo> getProfilePicture() {
        User user= userContext.getUser();
        if (!user.getEmail().isBlank()) {
            List<Photo> profilePhotos = user.getProfilePictures();
            if (!profilePhotos.isEmpty()) {


                return new ResponseEntity<>(profilePhotos.get(0), HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(HttpStatus.CONFLICT);
    }
    public ResponseEntity<ProfileDTO> getProfile() {

        User user = userContext.getUser();
        log.info("Get profile for email {}",user.getEmail());
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
                    user.getProfilePictures());
            return ResponseEntity.ok(dto);
            }
        }
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    public ResponseEntity<List<Photo>> uploadProfilePictures(List<MultipartFile> files) throws FileUploadFailedException {

        User user  = userContext.getUser();
            List<Photo> photos = new ArrayList<>();
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    try {
                        byte[] bytes = file.getBytes();
                        String fileName = "Document-" + user.getId() + "-" + file.getOriginalFilename();
                        String filePath = path + fileName;


                        // Ensure the directory exists
                        File dir = new File(path);
                        if (!dir.exists()) {
                            dir.mkdirs();
                        }

                        BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(new File(filePath)));
                        stream.write(bytes);
                        stream.close();


                        Photo photo = photoRepository.save(Photo.builder().name(fileName).path(filePath).build());
                        photos.add(photo);

                        log.info("Photo with id: "+photo.getId()+" is attenpting to be added to the database");
                        photoRepository.addPhotoByPhotoIdAndUserId(user.getId(), photo.getId());
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new FileUploadFailedException("File upload failed for file: " + file.getOriginalFilename());
                    }
                }
            }
            return ResponseEntity.ok(photos);

    }
    public ResponseEntity<ProfileDTO> getUserById(Long id){

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
                user.getProfilePictures());
        return new ResponseEntity<>(dto,HttpStatus.OK );
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
                    user.getProfilePictures());
            profileDTOs.add(dto);
        }
        return profileDTOs;
}
    public ResponseEntity<List<ProfileDTO>> getAllUsers() {
        return new ResponseEntity<>(mapToProfileDTO(userRepository.findAll()),HttpStatus.OK);
    }

    public ResponseEntity<?> deactivateUser(Long userId) {
        userRepository.updateStatusById("non-active",userId);
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<?> activateUser(Long userId) {
        userRepository.updateStatusById("active",userId);
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<?> deleteUser(Long userId) {
        userRepository.deleteById(userId);
        return ResponseEntity.ok().build();
    }
}
