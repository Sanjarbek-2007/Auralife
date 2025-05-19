package uz.project.jorasoft.domains;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
public class ActivisationCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;
    String recieverEmail;
    String code;
    Boolean aproved;
    String expityDateTime;
    String type;

    public ActivisationCode(String recieverEmail, String code, Boolean aproved, String expityDateTime, String type) {
        this.recieverEmail = recieverEmail;
        this.code = code;
        this.aproved = aproved;
        this.expityDateTime = expityDateTime;
        this.type = type;
    }



    public ActivisationCode() {

    }
}
