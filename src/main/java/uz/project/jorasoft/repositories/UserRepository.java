package uz.project.jorasoft.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.project.jorasoft.domains.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByPhoneNumber(String phoneNumber);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByEmail(String email);

}