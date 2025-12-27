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
public class CommunityPostFileId implements Serializable {

    private Long postId;
    private Long fileId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CommunityPostFileId)) return false;
        CommunityPostFileId that = (CommunityPostFileId) o;
        return Objects.equals(postId, that.postId)
                && Objects.equals(fileId, that.fileId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(postId, fileId);
    }

    public static CommunityPostFileId create(Long postId, Long fileId) {
        validateCreate(postId, fileId);
        return CommunityPostFileId.builder()
                .postId(postId)
                .fileId(fileId)
                .build();
    }

    private static void validateCreate(Long postId, Long fileId) {
        if (postId == null) {
            log.error("생성 오류 - postId : null");
            throw new CustomException(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
        }
        if (fileId == null) {
            log.error("생성 오류 - fileId : null");
            throw new CustomException(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
        }
    }
}
