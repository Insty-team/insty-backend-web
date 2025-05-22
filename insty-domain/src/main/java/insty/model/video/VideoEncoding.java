package insty.model.video;

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
@Builder
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

    // 임시 비활성화 - 넣기 까다로움
//    @Column(nullable = false, length = 20)
//    private String resolution;
//    private int duration;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
