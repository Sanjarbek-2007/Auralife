package uz.project.auralife.domains;

import jakarta.persistence.*;
import lombok.*;
import uz.project.auralife.domains.enums.Apps;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "connected_apps")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConnectedApp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Apps appName;

    @Column(nullable = false)
    private LocalDateTime authorizedAt;

    private LocalDateTime revokedAt;
}
