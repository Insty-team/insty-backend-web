package insty.model.video;

import insty.error.VideoErrorCode;
import insty.exception.CustomException;
import insty.model.BaseEntity;
import insty.model.community.CommunityComment;
import insty.model.user.User;
import insty.util.FileUtils;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Entity
@Table(name = "video_community_comments", schema = "web_service")
@Getter
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class VideoCommunityComment extends BaseEntity implements BaseVideo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID videoUuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_comment_id")
    private CommunityComment communityComment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String s3Key;

    @Column(nullable = false, length = 10)
    private String extension;

    @Column(nullable = false, length = 150)
    private String originalFileName;

    @Builder.Default
    @Column(nullable = false)
    private int duration = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    private EncodingStatus encodingStatus;

    private Instant encodingAt;

    @Builder.Default
    @Column(nullable = false)
    private boolean isDeleted = false;

    public static VideoCommunityComment create(String fileName, UUID uuid, User user) {
        validateCreate(fileName, uuid, user);
        String extension = FileUtils.extractExtension(fileName)
                .orElseThrow(() -> new CustomException(VideoErrorCode.VIDEO_INVALID_FILE_NAME));
        String s3BucketKey = getS3BucketKey(fileName, uuid);

        return VideoCommunityComment.builder()
                .videoUuid(uuid)
                .user(user)
                .s3Key(s3BucketKey)
                .extension(extension)
                .originalFileName(fileName)
                .encodingStatus(EncodingStatus.PROCESSING)
                .encodingAt(Instant.now())
                .build();
    }

    private static void validateCreate(String fileName, UUID uuid, User user) {
        if (fileName == null || fileName.trim().isEmpty()) {
            log.error("생성 오류 - fileName : 비었음");
            throw new CustomException(VideoErrorCode.VIDEO_CREATE_ERROR);
        }
        if (fileName.length() > 150) {
            log.error("생성 오류 - fileName : 150자가 초과됨");
            throw new CustomException(VideoErrorCode.VIDEO_CREATE_ERROR);
        }
        if (uuid == null) {
            log.error("생성 오류 - uuid : null");
            throw new CustomException(VideoErrorCode.VIDEO_CREATE_ERROR);
        }
        if (user == null || user.getId() == null) {
            log.error("생성 오류 - user : 유저 미지정 또는 유저 Id 미설정");
            throw new CustomException(VideoErrorCode.VIDEO_CREATE_ERROR);
        }
    }

    private static String getS3BucketKey(String fileName, UUID uuid) {
        String extension = FileUtils.extractExtension(fileName)
                .orElseThrow(() -> new CustomException(VideoErrorCode.VIDEO_INVALID_FILE_NAME));
        return "vod/" + VideoType.COMMUNITY_COMMENT + "/" + extension + "/" + uuid + "/" + fileName;
    }

    public void updateCommunityComment(CommunityComment communityComment) {
        this.communityComment = communityComment;
    }
}
