package uz.project.auralife.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.project.auralife.config.UserContext;
import uz.project.auralife.dtos.ProfileDTO;
import uz.project.auralife.services.ProfileService;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final UserContext userContext;

    private final ProfileService profileService;

    @GetMapping("/get-all-users")
    public ResponseEntity<List<ProfileDTO>> getUsers() {
        if(authoriseAdmin()) return profileService.getAllUsers();
        return ResponseEntity.badRequest().build();
    }
    @PutMapping("/activate-user")
    public ResponseEntity<?> activateUser(@RequestParam Long userId) {
        if(authoriseAdmin()) return profileService.activateUser(userId);
        return ResponseEntity.badRequest().build();
    }
    @PutMapping("/deactivate-user")
    public ResponseEntity<?> deactivateUser(@RequestParam Long userId) {
        if(authoriseAdmin()) return profileService.deactivateUser(userId);
        return ResponseEntity.badRequest().build();
    }
     @PutMapping("/delete-user")
    public ResponseEntity<?> deleteUser(@RequestParam Long userId) {
        if(authoriseAdmin()) return profileService.deleteUser(userId);
        return ResponseEntity.badRequest().build();
    }

    private Boolean authoriseAdmin(){
        return userContext.getUser().getRoles().stream().anyMatch(role -> role.getName().equals("ADMIN"));
    }
}
