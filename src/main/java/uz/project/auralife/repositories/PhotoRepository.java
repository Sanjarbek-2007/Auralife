package uz.project.auralife.repositories;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.project.auralife.domains.Photo;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {
    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "INSERT INTO users_profile_photos (user_id, profile_photos_id) values( :userId, :id )")
    void addPhotoByPhotoIdAndUserId(Long id, Long userId);
    boolean existsByPurposeName(String purposeName);

    Optional<Photo> findByPurposeName(String purposeName);
    @Modifying
    @Query("UPDATE User u SET u.profilePictures = :photos WHERE u.id = :userId")
    void updateProfilePictures(@Param("userId") Long userId, @Param("photos") List<Photo> photos);

}
