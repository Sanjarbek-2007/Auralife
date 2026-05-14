package uz.project.auralife.repositories;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import uz.project.auralife.domains.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByPhoneNumber(String phoneNumber);
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.email = :email")
    Optional<User> findByEmail(String email);

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
    @Query("UPDATE User u SET u.profilePhotoFileId = :profilePhotoFileId WHERE u.phoneNumber = :phoneNumber")
    void updateProfilePhotoFileIdByPhoneNumber(String phoneNumber, String profilePhotoFileId);


    @Transactional
    @Modifying
    @Query("update User u set u.password = ?1 where u.phoneNumber = ?2")
    void updatePasswordByPhoneNumber(String password, String phoneNumber);

    boolean existsByUsername(String username);

    @Override
    boolean existsById(Long aLong);

    @Query("update User u set u.firstName = :firstName where u.email = :email")
    @Modifying
    void updateFirstNameByEmail(String firstName, String email);

    @Query("update User u set u.lastName = :lastName where u.email = :email")
    @Modifying
    void updateLastNameByEmail(String lastName, String email);

    @Query("update User u set u.email = :email where u.email = :email1")
    @Modifying
    void updateEmailByEmail(String email, String email1);

    @Query("update User u set u.phoneNumber = :phoneNumber where u.email = :email")
    @Modifying
    void updatePhoneNumberByEmail(String phoneNumber, String email);

    @Transactional
    @Modifying
    @Query("update User u set u.status = ?1 where u.id = ?2")
    int updateStatusById(String status, Long id);

    @Transactional
    @Modifying
    @Query("update User u set u.username = ?1 where u.email = ?2")
    int updateUsernameByEmail(String username, String email);

    @Transactional
    @Modifying
    @Query("update User u set u.birthDate = ?1 where u.email = ?2")
    int updateBirthDateByEmail(Date birthDate, String email);

    Optional<User> findByUsername(String username);

    List<User> findByUsernameContainingIgnoreCase(String username);

    @Transactional
    @Modifying
    @Query("update User u set u.lastActiveTime = current_timestamp, u.activityCount = coalesce(u.activityCount, 0) + 1 where u.email = :email")
    void recordActivityByEmail(@Param("email") String email);

    @Transactional
    @Modifying
    @Query("update User u set u.isBanned = :isBanned where u.id = :id")
    void updateBanStatusById(@Param("id") Long id, @Param("isBanned") Boolean isBanned);
}