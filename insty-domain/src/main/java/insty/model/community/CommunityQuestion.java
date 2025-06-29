package insty.model.community;

import insty.error.CommunityErrorCode;
import insty.exception.CustomException;
import insty.model.BaseEntity;
import insty.model.course.Course;
import insty.model.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Entity
@Table(name = "community_questions", schema = "web_service")
@Getter
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class CommunityQuestion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "communityQuestion", cascade = CascadeType.PERSIST, orphanRemoval = true)
    @Builder.Default
    private List<CommunityFile> attachments = new ArrayList<>();

    @OneToMany(mappedBy = "communityQuestion", cascade = CascadeType.PERSIST, orphanRemoval = true)
    @Builder.Default
    private List<CommunityAnswer> answers = new ArrayList<>();

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false, name = "is_answered")
    private boolean isAnswered;

    @CreatedDate
    @Column(nullable = false, name = "created_at", updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false, name = "updated_at", updatable = false)
    private Instant updatedAt;

    @Column(nullable = false, name = "is_deleted")
    private boolean isDeleted;

    public static CommunityQuestion create(Course course, User user, String title, String content) {
        validateCreate(course, user, title, content);
        return CommunityQuestion.builder()
                .course(course)
                .user(user)
                .title(title)
                .content(content)
                .isAnswered(false)
                .isDeleted(false)
                .build();
    }

    public void update(String title, String content, List<CommunityFile> attachments) {
        this.title = title;
        this.content = content;
        this.attachments = attachments;
        this.updatedAt = Instant.now();
    }

    private static void validateCreate(Course course, User user, String title, String content) {
        if (course == null || course.getId() == null) {
            log.error("CommunityQuestion creation error - course is null or has no ID");
            throw new CustomException(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
        }
        if (user == null || user.getId() == null) {
            log.error("CommunityQuestion creation error - user is null or has no ID");
            throw new CustomException(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
        }
        if (title == null || title.isBlank()) {
            log.error("CommunityQuestion creation error - title is null or blank");
            throw new CustomException(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
        }
        if (content == null || content.isBlank()) {
            log.error("CommunityQuestion creation error - content is null or blank");
            throw new CustomException(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
        }
    }
}
