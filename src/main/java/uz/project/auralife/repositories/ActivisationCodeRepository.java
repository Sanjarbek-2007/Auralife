package uz.project.auralife.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uz.project.auralife.domains.ActivisationCode;

public interface ActivisationCodeRepository extends JpaRepository<ActivisationCode, Long> {
    Optional<ActivisationCode> findByRecieverEmail(String recieverEmail);

    @Query("select (count(a) > 0) from ActivisationCode a where a.recieverEmail = ?1 and a.type = ?2")
    boolean existsByRecieverEmailAndType(String recieverEmail, String type);

    @Query("select a from ActivisationCode a where a.recieverEmail = ?1 and a.type = ?2")
    Optional<ActivisationCode> findByRecieverEmailAndType(String recieverEmail, String type);
}