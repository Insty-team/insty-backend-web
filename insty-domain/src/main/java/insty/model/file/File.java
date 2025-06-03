package insty.model.file;

import insty.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "files", schema = "web_service",
        indexes = {
                @Index(name = "container_index", columnList = "containerType, containerId")
        }
)
@Getter
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class File extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private FileContainerType containerType;

    @Column(nullable = false)
    private Long containerId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String originalName;

    @Column(nullable = false, length = 100)
    private String contentType;

    @Column(nullable = false)
    private long size;


    public static File create(FileContainerType containerType, Long containerId, String name, String originalName,
                              String contentType, long size) {
        return File.builder()
                .containerType(containerType)
                .containerId(containerId)
                .name(name)
                .originalName(originalName)
                .contentType(contentType)
                .size(size)
                .build();
    }

    public String getUrl(String domain) {
        return "https://" + domain + "/file/" + contentType + "/" + containerId + "/" + name;
    }
}
