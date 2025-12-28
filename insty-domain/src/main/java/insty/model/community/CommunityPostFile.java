package insty.model.community;

import insty.error.CommunityErrorCode;
import insty.exception.CustomException;
import insty.model.BaseEntity;
import insty.model.community.id.CommunityPostFileId;
import insty.model.file.File;
import jakarta.persistence.CascadeType;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Entity
@Table(name = "community_post_files", schema = "web_service")
@Getter
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class CommunityPostFile extends BaseEntity {

    @EmbeddedId
    private CommunityPostFileId communityPostFileId;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("postId")
    @JoinColumn(name = "post_id", nullable = false)
    private CommunityPost communityPost;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST, orphanRemoval = true)
    @MapsId("fileId")
    @JoinColumn(name = "file_id", nullable = false)
    private File file;

    public static CommunityPostFile create(CommunityPost communityPost, File file) {
        validateCreate(communityPost, file);
        return CommunityPostFile.builder()
                .communityPostFileId(CommunityPostFileId.create(communityPost.getId(), file.getId()))
                .communityPost(communityPost)
                .file(file)
                .build();
    }

    private static void validateCreate(CommunityPost communityPost, File file) {
        if (communityPost == null) {
            log.error("생성 오류 - communityPost : null");
            throw new CustomException(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
        }
        if (file == null) {
            log.error("생성 오류 - file : null");
            throw new CustomException(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
        }
    }
}
