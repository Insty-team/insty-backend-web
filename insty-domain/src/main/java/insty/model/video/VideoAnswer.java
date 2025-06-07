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
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "video_answers", schema = "web_service")
@Getter
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class VideoAnswer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID videoUuid;

    // TODO - 커뮤니티 답변 테이블 추가 시 객체로 변경
    private Long communityQuestionId;

    @Column(nullable = false, length = 100)
    private String s3Key;

    @Column(nullable = false, length = 10)
    private String extension;

    @Column(nullable = false)
    private String originalFileName;

    @Builder.Default
    @Column(nullable = false)
    private int duration = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    private EncodingStatus encodingStatus;

    private Instant encodingAt;

    private boolean isDeleted;


    public static VideoAnswer create(String fileName, UUID uuid) {
        String extension = FileUtils.extractExtension(fileName)
                .orElseThrow(() -> new CustomException(VideoErrorCode.VIDEO_INVALID_FILE_NAME));
        String s3BucketKey = getS3BucketKey(fileName, uuid);

        return VideoAnswer.builder()
                .videoUuid(uuid)
                .s3Key(s3BucketKey)
                .extension(extension)
                .originalFileName(fileName)
                .encodingStatus(EncodingStatus.PROCESSING)
                .encodingAt(Instant.now()) // 비용 문제로 영상 삽입 시 인코딩 시작했다고 가정
                .build();
    }

    /**
     * s3 객체 키에 대응되는 문자열을 반환한다.
     *
     * @param fileName 파일명 fileName.mp4
     * @param uuid
     * @return vod/ANSWER/mp4/uuid/fileName.mp4
     */
    private static String getS3BucketKey(String fileName, UUID uuid) {
        String extension = FileUtils.extractExtension(fileName)
                .orElseThrow(() -> new CustomException(VideoErrorCode.VIDEO_INVALID_FILE_NAME));
        return "vod/" + VideoType.ANSWER + "/" + extension + "/" + uuid + "/" + fileName;
    }
}
