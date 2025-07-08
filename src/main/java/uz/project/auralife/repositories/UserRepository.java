package uz.project.auralife.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import uz.project.auralife.domains.User;

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

    @Transactional
    @Modifying
    @Query("update User u set u.firstName = ?1 where u.phoneNumber = ?2")
    int updateFirstNameByPhoneNumber(String firstName, String phoneNumber);
    @Transactional
    @Modifying
    @Query("update User u set u.lastName = ?1 where u.phoneNumber = ?2")
    int updateLastNameByPhoneNumber(String lastName, String phoneNumber);


    @Modifying
    @Query("UPDATE User u SET u.email = :email WHERE u.phoneNumber = :phoneNumber")
    void updateEmailByPhoneNumber(String phoneNumber, String email);

    @Modifying
    @Query("UPDATE User u SET u.phoneNumber = :newPhoneNumber WHERE u.phoneNumber = :currentPhoneNumber")
    void updatePhoneNumberByPhoneNumber(String currentPhoneNumber, String newPhoneNumber);

    @Modifying
    @Query("UPDATE User u SET u.profilePictures = :profilePicture WHERE u.phoneNumber = :phoneNumber")
    void updateProfilePictureByPhoneNumber(String phoneNumber, String profilePicture);


    @Transactional
    @Modifying
    @Query("update User u set u.password = ?1 where u.phoneNumber = ?2")
    void updatePasswordByPhoneNumber(String password, String phoneNumber);

    boolean existsByUsername(String username);

    @Override
    boolean existsById(Long aLong);
}