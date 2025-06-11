package insty.model.file;

import insty.error.FileErrorCode;
import insty.exception.CustomException;
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
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
        validateCreate(containerType, containerId, name, originalName, contentType, size);
        return File.builder()
                .containerType(containerType)
                .containerId(containerId)
                .name(name)
                .originalName(originalName)
                .contentType(contentType)
                .size(size)
                .build();
    }

    private static void validateCreate(FileContainerType containerType, Long containerId, String name,
                                       String originalName, String contentType, long size) {
        if (containerType == null) {
            log.error("생성 오류 - containerType : null");
            throw new CustomException(FileErrorCode.FILE_CREATE_ERROR);
        }
        if (containerId == null) {
            log.error("생성 오류 - containerId : null");
            throw new CustomException(FileErrorCode.FILE_CREATE_ERROR);
        }
        if (name == null || name.trim().isEmpty()) {
            log.error("생성 오류 - name : 비었음");
            throw new CustomException(FileErrorCode.FILE_CREATE_ERROR);
        }
        if (originalName == null || originalName.trim().isEmpty()) {
            log.error("생성 오류 - originalName : 비었음");
            throw new CustomException(FileErrorCode.FILE_CREATE_ERROR);
        }
        if (contentType == null || contentType.trim().isEmpty()) {
            log.error("생성 오류 - contentType : 비었음");
            throw new CustomException(FileErrorCode.FILE_CREATE_ERROR);
        }
        if (size < 0) {
            log.error("생성 오류 - size : {}", size);
            throw new CustomException(FileErrorCode.FILE_CREATE_ERROR);
        }
    }

    public String getUrl(String domain) {
        return "https://" + domain + "/file/" + containerType + "/" + containerId + "/" + name;
    }
}
