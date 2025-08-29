package uz.project.auralife.domains;

import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class Photo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    private String path;
    private String name;
    private String url;
    private String source;

    @Nullable
    private String purposeName;

    public Photo(String path, String name, String url, String source, @Nullable String purposeName) {
        this.path = path;
        this.name = name;
        this.url = url;
        this.source = source;
        this.purposeName = purposeName;
    }
}
