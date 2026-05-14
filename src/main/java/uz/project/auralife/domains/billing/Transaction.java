package uz.project.auralife.domains.billing;

import jakarta.persistence.*;
import lombok.*;
import uz.project.auralife.domains.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Tarrifs tariff;

    @Column(nullable = false)
    private Long amount; // in minor units (tiyin), e.g. 5000000 = 50000 UZS

    @Column(nullable = false)
    private String currency;

    /** The transaction ID assigned by Click when a payment is initiated */
    private String clickTransId;

    /** The ID we send to Click as merchant_trans_id (our internal pending tx id) */
    private String merchantTransId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime paidAt;

    public enum TransactionStatus {
        PENDING, PAID, CANCELLED
    }
}
