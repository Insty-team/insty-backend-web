package insty.model.video;

import insty.error.VideoErrorCode;
import insty.exception.CustomException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "video_encodings", schema = "web_service")
@Getter
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class VideoEncoding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private UUID videoUuid;

    @Column(nullable = false, length = 10)
    private String format;

    @Column(name = "encoding_s3_key", nullable = false, length = 1000)
    private String encodingS3Key;

    // TODO 우선순위 미룸 - 넣기 까다로움
//    @Column(nullable = false, length = 20)
//    private String resolution;
//    private int duration;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;


    /**
     * 인코딩 영상의 디렉토리 경로를 반환한다.
     *
     * @return vod/{type}/hls/{uuid}
     */
    public String getEncodingVideoDirectoryPath() {
        validateEncodingS3Key();
        int lastSlashIndex = this.encodingS3Key.lastIndexOf('/');
        return this.encodingS3Key.substring(0, lastSlashIndex);
    }

    /**
     * HLS 영상의 마스터 파일 경로를 반환한다.
     *
     * @return vod/{type}/hls/{uuid}/fileName.m3u8
     */
    public String getHlsMasterFileKey() {
        validateEncodingS3Key();
        return this.encodingS3Key + ".m3u8";
    }

    /**
     * s3 키의 형식을 검사한다.<br> 키의 형식은 vod/{type}/hls/{uuid}/{파일명} 으로 /가 4개 들어가야 한다.
     */
    public void validateEncodingS3Key() {
        int slashCount = (int) this.encodingS3Key.chars()
                .filter(c -> c == '/')
                .count();
        if (slashCount != 4) {
            throw new CustomException(VideoErrorCode.VIDEO_INVALID_ENCODING_KEY);
        }
    }
}
