package uz.project.auralife.domains;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "devices")
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;
    private Long userId;
    private Boolean idPrimary;
    private String iotDeviceId;
    private String deviceName;
    private String deviceType;
    private String permittedApps;
    private LocalDateTime joinedTime;
    private LocalDateTime lastActivityTime;
    private String location;

    public Device(Long userId, Boolean idPrimary, String iotDeviceId, String deviceName, String deviceType, String permittedApps, LocalDateTime joinedTime, LocalDateTime lastActivityTime, String location) {
        this.userId = userId;
        this.idPrimary = idPrimary;
        this.iotDeviceId = iotDeviceId;
        this.deviceName = deviceName;
        this.deviceType = deviceType;
        this.permittedApps = permittedApps;
        this.joinedTime = joinedTime;
        this.lastActivityTime = lastActivityTime;
        this.location = location;
    }
}
