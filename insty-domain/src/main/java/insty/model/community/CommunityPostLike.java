package insty.model.community;

import insty.error.CommunityErrorCode;
import insty.exception.CustomException;
import insty.model.BaseEntity;
import insty.model.user.User;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Index;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Entity
@Table(
        name = "community_post_likes",
        schema = "web_service",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_community_post_likes_post_user", columnNames = {"post_id", "user_id"})
        },
        indexes = {
                @Index(name = "idx_community_post_likes_post_id", columnList = "post_id"),
                @Index(name = "idx_community_post_likes_user_id", columnList = "user_id")
        }
)
@Getter
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CommunityPostLike extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private CommunityPost communityPost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public static CommunityPostLike create(CommunityPost post, User user) {
        validateCreate(post, user);
        return CommunityPostLike.builder()
                .communityPost(post)
                .user(user)
                .build();
    }

    private static void validateCreate(CommunityPost post, User user) {
        if (post == null || post.getId() == null) {
            log.error("생성 오류 - post : null");
            throw new CustomException(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
        }
        if (user == null || user.getId() == null) {
            log.error("생성 오류 - user : null");
            throw new CustomException(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
        }
    }
}
