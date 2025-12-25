package insty.model.courseqna;

import insty.error.CommunityErrorCode;
import insty.exception.CustomException;
import insty.model.courseqna.id.CourseQuestionViewId;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Entity
@Table(name = "community_question_views", schema = "web_service")
@Getter
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseQuestionView {

    @EmbeddedId
    private CourseQuestionViewId courseQuestionViewId;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("courseQuestion")
    @JoinColumn(name = "question_id", nullable = false)
    private CourseQuestion courseQuestion;

    @Column(name = "last_viewed_at", nullable = false)
    private Instant lastViewedAt;

    public static CourseQuestionView create(CourseQuestion courseQuestion, Long userId) {
        validateCreate(courseQuestion, userId);
        return CourseQuestionView.builder()
                .courseQuestionViewId(CourseQuestionViewId.create(courseQuestion.getId(), userId))
                .courseQuestion(courseQuestion)
                .lastViewedAt(Instant.now())
                .build();
    }

    public void updateLastViewedAt() {
        this.lastViewedAt = Instant.now();
    }

    private static void validateCreate(CourseQuestion courseQuestion, Long userId) {
        if (courseQuestion == null) {
            log.error("생성 오류 - courseQuestion : null");
            throw new CustomException(CommunityErrorCode.COURSE_CREATE_ERROR);
        }
        if (userId == null) {
            log.error("생성 오류 - userId : null");
            throw new CustomException(CommunityErrorCode.COURSE_CREATE_ERROR);
        }
    }
}
