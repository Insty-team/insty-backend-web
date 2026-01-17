package insty.model.community;

import insty.error.CommunityErrorCode;
import insty.exception.CustomException;
import insty.model.BaseEntity;
import insty.model.course.Course;
import insty.model.user.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
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
@Table(
        name = "community_posts",
        schema = "web_service",
        indexes = {
                @Index(name = "idx_community_posts_course_id_is_deleted", columnList = "course_id, is_deleted"),
                @Index(name = "idx_community_posts_user_id_is_deleted", columnList = "user_id, is_deleted")
        }
)
@Getter
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class CommunityPost extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "communityPost", cascade = CascadeType.PERSIST, orphanRemoval = true)
    @Builder.Default
    private List<CommunityPostFile> attachments = new ArrayList<>();

    @OneToMany(mappedBy = "communityPost", cascade = CascadeType.PERSIST, orphanRemoval = true)
    @Builder.Default
    private List<CommunityComment> comments = new ArrayList<>();

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Builder.Default
    @Column(nullable = false)
    private int likeCount = 0;

    @Column(nullable = false, name = "is_deleted")
    private boolean isDeleted;

    public static CommunityPost create(User user, Course course, String title, String content) {
        validateCreate(user, course, title, content);
        return CommunityPost.builder()
                .course(course)
                .user(user)
                .title(title)
                .content(content)
                .isDeleted(false)
                .build();
    }

    private static void validateCreate(User user, Course course, String title, String content) {
        if (user == null || user.getId() == null) {
            log.error("생성 오류 - user : null");
            throw new CustomException(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
        }
        if (course == null || course.getId() == null) {
            log.error("생성 오류 - course : null");
            throw new CustomException(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
        }
        if (title == null || title.isBlank()) {
            log.error("생성 오류 - title : 비었음");
            throw new CustomException(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
        }
        if (content == null || content.isBlank()) {
            log.error("생성 오류 - content : 비었음");
            throw new CustomException(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
        }
    }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void removeAllFiles() {
        this.attachments.clear();
    }

    public void markAsDeleted() {
        isDeleted = true;
    }
}
