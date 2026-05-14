package uz.project.auralife.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.project.auralife.domains.ConnectedApp;
import uz.project.auralife.domains.enums.Apps;
import uz.project.auralife.repositories.ConnectedAppRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConnectedAppService {

    private final ConnectedAppRepository connectedAppRepository;

    public List<ConnectedApp> getConnectedApps(Long userId) {
        return connectedAppRepository.findAllByUserIdAndRevokedAtIsNull(userId);
    }

    public void revokeApp(Long userId, String appName) {
        Apps app = Apps.valueOf(appName.toUpperCase());
        connectedAppRepository.findByUserIdAndAppNameAndRevokedAtIsNull(userId, app)
                .ifPresent(connectedApp -> {
                    connectedApp.setRevokedAt(LocalDateTime.now());
                    connectedAppRepository.save(connectedApp);
                });
    }

    public void registerApp(Long userId, Apps app) {
        if (connectedAppRepository.findByUserIdAndAppNameAndRevokedAtIsNull(userId, app).isEmpty()) {
            ConnectedApp connectedApp = ConnectedApp.builder()
                    .userId(userId)
                    .appName(app)
                    .authorizedAt(LocalDateTime.now())
                    .build();
            connectedAppRepository.save(connectedApp);
        }
    }
}
