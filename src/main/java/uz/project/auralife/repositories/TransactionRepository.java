package uz.project.auralife.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.project.auralife.domains.billing.Transaction;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<Transaction> findByMerchantTransId(String merchantTransId);
}
