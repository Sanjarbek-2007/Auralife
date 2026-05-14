package uz.project.auralife.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uz.project.auralife.config.UserContext;
import uz.project.auralife.domains.ConnectedApp;
import uz.project.auralife.domains.Device;
import uz.project.auralife.exceptions.FileUploadFailedException;
import uz.project.auralife.domains.User;
import uz.project.auralife.dtos.UpdateProfileRequest;
import uz.project.auralife.services.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/account")
@RequiredArgsConstructor
@Slf4j
public class AccountController {

    private final UserContext userContext;
    private final ProfileService profileService;
    private final TwoFactorService twoFactorService;
    private final DeviceService deviceService;
    private final ConnectedAppService connectedAppService;

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        return profileService.getProfile();
    }

    @PostMapping("/profile/photo")
    public ResponseEntity<?> uploadPhoto(@RequestParam("file") MultipartFile file) throws FileUploadFailedException {
        // Will be implemented in ProfileService to call FilesAPI
        return profileService.uploadProfilePicture(file);
    }

    @PostMapping("/profile/update")
    public ResponseEntity<User> updateProfile(@RequestBody UpdateProfileRequest request) {
        return profileService.updateProfile(request);
    }

    @GetMapping("/devices")
    public ResponseEntity<List<Device>> getDevices() {
        List<Device> devices = deviceService.getDevicesForUser(userContext.getUserId());
        String currentDeviceId = userContext.getIotDeviceId();
        devices.forEach(d -> {
            if (currentDeviceId != null && currentDeviceId.equals(d.getIotDeviceId())) {
                d.setCurrent(true);
            }
        });
        return ResponseEntity.ok(devices);
    }

    @DeleteMapping("/devices/{id}")
    public ResponseEntity<Void> revokeDevice(@PathVariable Long id) {
        deviceService.revokeDevice(userContext.getUserId(), id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/devices/others")
    public ResponseEntity<Void> revokeOthers() {
        deviceService.revokeAllOthers(userContext.getUserId(), userContext.getIotDeviceId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/apps")
    public ResponseEntity<List<ConnectedApp>> getApps() {
        return ResponseEntity.ok(connectedAppService.getConnectedApps(userContext.getUserId()));
    }

    @DeleteMapping("/apps/{appName}")
    public ResponseEntity<Void> revokeApp(@PathVariable String appName) {
        connectedAppService.revokeApp(userContext.getUserId(), appName);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/security/2fa/setup")
    public ResponseEntity<?> setup2fa() {
        String secret = twoFactorService.generateSecret(userContext.getUserId());
        String qrUrl = twoFactorService.getQrCodeUrl(userContext.getUserId());
        return ResponseEntity.ok(Map.of("secret", secret, "qrCodeUrl", qrUrl));
    }

    @PostMapping("/security/2fa/enable")
    public ResponseEntity<Void> enable2fa(@RequestParam int code) {
        twoFactorService.enableTwoFactor(userContext.getUserId(), code);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/security/2fa")
    public ResponseEntity<Void> disable2fa(@RequestParam int code) {
        twoFactorService.disableTwoFactor(userContext.getUserId(), code);
        return ResponseEntity.noContent().build();
    }
}
