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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accepted_answer_id", nullable = true)
    private CommunityAnswer acceptedAnswer;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false, name = "is_answered")
    private boolean isAnswered;

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

    private static void validateCreate(Course course, User user, String title, String content) {
        if (course == null || course.getId() == null) {
            log.error("생성 오류 - course : null");
            throw new CustomException(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
        }
        if (user == null || user.getId() == null) {
            log.error("생성 오류 - user : null");
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

    public void update(String title, String content, List<CommunityFile> attachments) {
        this.title = title;
        this.content = content;
        this.attachments = attachments;
    }

    public void acceptAnswer(CommunityAnswer answer) {
        if (this.acceptedAnswer != null) {
            this.acceptedAnswer.unaccept();
        }
        this.acceptedAnswer = answer;
        this.isAnswered = true;
        answer.accept();
    }

    public void unacceptAnswer() {
        if (this.acceptedAnswer != null) {
            this.acceptedAnswer.unaccept();
            this.acceptedAnswer = null;
        }
        this.isAnswered = false;
    }

    public void markAsDeleted() {
        isDeleted = true;
    }

}
