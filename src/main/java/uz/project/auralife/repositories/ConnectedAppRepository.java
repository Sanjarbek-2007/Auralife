package uz.project.auralife.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.project.auralife.domains.ConnectedApp;
import uz.project.auralife.domains.enums.Apps;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConnectedAppRepository extends JpaRepository<ConnectedApp, Long> {
    List<ConnectedApp> findAllByUserIdAndRevokedAtIsNull(Long userId);
    Optional<ConnectedApp> findByUserIdAndAppNameAndRevokedAtIsNull(Long userId, Apps appName);
}
