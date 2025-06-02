package uz.project.auralife.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
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

    @Value("${api.photo-save-path}")
    private  String path = new String();



    @Transactional
    public ResponseEntity<User> editFullname(String firstName, String lastName) {
        String phoneNumber = SecurityContextHolder.getContext().getAuthentication().getName();
        userRepository.updateFirstNameByPhoneNumber(firstName,phoneNumber );
        userRepository.updateLastNameByPhoneNumber(lastName,phoneNumber);
        return ResponseEntity.ok(userRepository.findByPhoneNumber(phoneNumber).get());
    }

    public ResponseEntity<String> editPassword(String password) {
        String phoneNumber = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!phoneNumber.isBlank()) {
            String encodedPassword = passwordEncoder.encode(password);
            userRepository.updatePasswordByPhoneNumber(phoneNumber, encodedPassword);
            return new ResponseEntity<>("Password edited successfully", HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
    @Transactional
    public ResponseEntity<User> editEmail(String email) {
        String phoneNumber = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!phoneNumber.isBlank()) {
            userRepository.updateEmailByPhoneNumber(phoneNumber, email);
            User updatedUser = userRepository.findByPhoneNumber(phoneNumber).orElse(null);
            return new ResponseEntity<>(updatedUser, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    @Transactional
    public ResponseEntity<User> editPhoneNumber(String phoneNumber) {
        String currentPhoneNumber = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!currentPhoneNumber.isBlank()) {
            userRepository.updatePhoneNumberByPhoneNumber(currentPhoneNumber, phoneNumber);
            User updatedUser = userRepository.findByPhoneNumber(phoneNumber).orElse(null);
            return new ResponseEntity<>(updatedUser, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    public ResponseEntity<Photo> getProfilePicture() {
        String phoneNumber = SecurityContextHolder.getContext().getAuthentication().getName();

        if (!phoneNumber.isBlank()) {
            User user = userRepository.findByPhoneNumber(phoneNumber).orElse(null);
            List<Photo> profilePhotos = user.getProfilePictures();
            if (!profilePhotos.isEmpty()) {


                return new ResponseEntity<>(profilePhotos.get(0), HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(HttpStatus.CONFLICT);
    }
    public ResponseEntity<ProfileDTO> getProfile() {

        String phoneNumber = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Get profile for phone number {}", phoneNumber);
        if (!phoneNumber.isBlank()) {
            User user = userRepository.findByPhoneNumber(phoneNumber).get();
            ProfileDTO dto = new ProfileDTO(
                    user.getId(),
                    user.getFirstName(),
                    user.getLastName(),
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
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    public ResponseEntity<List<Photo>> uploadProfilePictures(List<MultipartFile> files) throws FileUploadFailedException {

        String phoneNumber = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByPhoneNumber(phoneNumber).orElse(null);
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
}
