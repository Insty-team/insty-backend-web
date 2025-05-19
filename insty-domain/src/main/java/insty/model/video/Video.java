package insty.model.video;

import insty.error.VideoErrorCode;
import insty.exception.CustomException;
import insty.model.BaseEntity;
import insty.util.FileUtils;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "videos", schema = "shared")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Video extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID videoUuid;

    private Long courseId;

    @Column(length = 100)
    private String s3Key;

    @Column(nullable = false, length = 10)
    private String extension;

    @Column(nullable = false)
    private String originalFileName;

    @Column(length = 1000)
    private String thumbnailUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    private EncodingStatus encodingStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    private AnalysisStatus analysisStatus;


    public static Video create(String fileName) {
        String extension = FileUtils.extractExtension(fileName)
                .orElseThrow(() -> new CustomException(VideoErrorCode.VIDEO_INVALID_FILE_NAME));

        return Video.builder()
                .videoUuid(UUID.randomUUID())
                .extension(extension)
                .originalFileName(fileName)
                .encodingStatus(EncodingStatus.WAITING)
                .analysisStatus(AnalysisStatus.WAITING)
                .build();
    }
}
