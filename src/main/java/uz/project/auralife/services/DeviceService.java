package uz.project.auralife.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.project.auralife.domains.Device;
import uz.project.auralife.repositories.DeviceRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;

    public List<Device> getDevicesForUser(Long userId) {
        return deviceRepository.findByUserId(userId);
    }

    public void revokeDevice(Long userId, Long deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("Device not found"));
        if (!device.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        deviceRepository.delete(device);
    }

    public void revokeAllOthers(Long userId, String currentIotId) {
        List<Device> devices = deviceRepository.findByUserId(userId);
        devices.stream()
                .filter(d -> !d.getIotDeviceId().equals(currentIotId))
                .forEach(deviceRepository::delete);
    }
}
