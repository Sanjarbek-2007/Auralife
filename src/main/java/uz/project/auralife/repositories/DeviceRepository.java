package uz.project.auralife.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.project.auralife.domains.Device;

import java.util.List;
import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Long> {
    Optional<Device> findByUserIdAndDeviceNameAndDeviceTypeAndPermittedApps(
            Long userId,
            String deviceName,
            String deviceType,
            String permittedApps
    );

    Optional<Device> findByIotDeviceId(String iotDeviceId);


    long deleteByIotDeviceId(String iotDeviceId);

    List<Device> findByUserId(Long userId);
}