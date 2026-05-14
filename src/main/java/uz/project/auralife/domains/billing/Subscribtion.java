package uz.project.auralife.domains.billing;

import jakarta.persistence.*;
import lombok.*;
import uz.project.auralife.domains.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "subscribtions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscribtion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    private Tarrifs tarrif;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private String countryCode;

    private LocalDateTime nextPaymentDate;

    @Builder.Default
    private Boolean isActive = false;
}

