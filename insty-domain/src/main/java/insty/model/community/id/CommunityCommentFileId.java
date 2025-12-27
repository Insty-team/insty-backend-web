package insty.model.community.id;

import insty.error.CommunityErrorCode;
import insty.exception.CustomException;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Embeddable
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CommunityCommentFileId implements Serializable {

    private Long commentId;
    private Long fileId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CommunityCommentFileId)) return false;
        CommunityCommentFileId that = (CommunityCommentFileId) o;
        return Objects.equals(commentId, that.commentId)
                && Objects.equals(fileId, that.fileId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commentId, fileId);
    }

    public static CommunityCommentFileId create(Long commentId, Long fileId) {
        validateCreate(commentId, fileId);
        return CommunityCommentFileId.builder()
                .commentId(commentId)
                .fileId(fileId)
                .build();
    }

    private static void validateCreate(Long commentId, Long fileId) {
        if (commentId == null) {
            log.error("생성 오류 - commentId : null");
            throw new CustomException(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
        }
        if (fileId == null) {
            log.error("생성 오류 - fileId : null");
            throw new CustomException(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
        }
    }
}
