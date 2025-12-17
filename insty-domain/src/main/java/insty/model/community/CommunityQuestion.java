package insty.model.community;

import insty.error.CommunityErrorCode;
import insty.exception.CustomException;
import insty.model.BaseEntity;
import insty.model.course.Course;
import insty.model.user.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
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
    private List<CommunityQuestionFile> attachments = new ArrayList<>();

    @OneToMany(mappedBy = "communityQuestion", cascade = CascadeType.PERSIST, orphanRemoval = true)
    @Builder.Default
    private List<CommunityAnswer> answers = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accepted_answer_id")
    private CommunityAnswer acceptedAnswer;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "board_type", nullable = false)
    private CommunityBoardType boardType;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "status")
    private QuestionStatus status;

    @Column(nullable = false, name = "is_deleted")
    private boolean isDeleted;

    public static CommunityQuestion create(Course course, User user, String title, String content,
                                           CommunityBoardType boardType) {
        validateCreate(course, user, title, content);
        return CommunityQuestion.builder()
                .course(course)
                .user(user)
                .title(title)
                .content(content)
                .status(QuestionStatus.WAITING)
                .isDeleted(false)
                .boardType(boardType != null ? boardType : CommunityBoardType.QNA)
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

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void acceptAnswer(CommunityAnswer answer) {
        if (this.acceptedAnswer != null) {
            this.acceptedAnswer.unaccept();
        }
        this.status = QuestionStatus.ACCEPTED;
        this.acceptedAnswer = answer;
        
        answer.accept();
    }

    public void unacceptAnswer() {
        if (this.acceptedAnswer != null) {
            this.acceptedAnswer.unaccept();
            this.acceptedAnswer = null;
        }
        this.status = QuestionStatus.ANSWERED;
        
    }

    public void changeStatusByAnswer(boolean hasAnswer) {
        // 이미 채택된 답변이 있는 경우 채택 상태를 유지
        if (this.acceptedAnswer != null && this.status == QuestionStatus.ACCEPTED) {
            return;
        }
        
        if (hasAnswer) {
            this.status = QuestionStatus.ANSWERED;
        } else {
            this.status = QuestionStatus.WAITING;
        }
    }

    public void handleAcceptedAnswerDeleted(boolean hasRemainingAnswers) {
        if (this.acceptedAnswer != null) {
            this.acceptedAnswer.unaccept();
            this.acceptedAnswer = null;
        }
        if (hasRemainingAnswers) {
            this.status = QuestionStatus.ANSWERED;
        } else {
            this.status = QuestionStatus.WAITING;
        }
        
    }

    public void removeAllFiles() {
        this.attachments.clear();
    }

    public void markAsDeleted() {
        isDeleted = true;
    }

}
