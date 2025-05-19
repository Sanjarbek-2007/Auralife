package uz.project.jorasoft.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import uz.project.jorasoft.domains.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByPhoneNumber(String phoneNumber);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByEmail(String email);

    @Transactional
    @Modifying
    @Query("update User u set u.status = ?1 where u.email = ?2")
    int updateStatusByEmail(String status, String email);

    @Transactional
    @Modifying
    @Query("update User u set u.password = ?1 where u.email = ?2")
    int updatePasswordByEmail(String password, String email);

    Optional<User> findByEmail(String email);

    boolean existsByEmailAndStatusIgnoreCase(String email, String status);


}