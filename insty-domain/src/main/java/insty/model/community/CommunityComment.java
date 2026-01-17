package insty.model.community;

import insty.error.CommunityErrorCode;
import insty.exception.CustomException;
import insty.model.BaseEntity;
import insty.model.user.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Entity
@Table(name = "community_comments", schema = "web_service")
@Getter
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class CommunityComment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private CommunityPost communityPost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String content;

    @OneToMany(mappedBy = "communityComment", cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true)
    @Builder.Default
    private List<CommunityCommentFile> attachments = new ArrayList<>();

    @Builder.Default
    @Column(nullable = false)
    private int likeCount = 0;

    @Column(nullable = false, name = "is_deleted")
    private boolean isDeleted;

    public static CommunityComment create(CommunityPost communityPost, User user, String content) {
        validateCreate(communityPost, user, content);
        return CommunityComment.builder()
                .communityPost(communityPost)
                .user(user)
                .content(content)
                .isDeleted(false)
                .build();
    }

    private static void validateCreate(CommunityPost communityPost, User user, String content) {
        if (communityPost == null) {
            log.error("생성 오류 - communityPost : null");
            throw new CustomException(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
        }
        if (user == null) {
            log.error("생성 오류 - user : null");
            throw new CustomException(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
        }
        if (content == null || content.trim().isEmpty()) {
            log.error("생성 오류 - content : 비었음");
            throw new CustomException(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
        }
    }

    public void update(String content) {
        this.content = content;
    }

    public void removeAllFiles() {
        this.attachments.clear();
    }

    public void markAsDeleted() {
        isDeleted = true;
    }
}
