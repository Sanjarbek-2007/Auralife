package uz.project.auralife.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.project.auralife.domains.billing.Subscribtion;

import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscribtion, Long> {
    Optional<Subscribtion> findByUserId(Long userId);
}
