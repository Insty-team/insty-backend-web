package insty.model.course;

import insty.error.CourseProgressErrorCode;
import insty.exception.CustomException;
import insty.model.BaseEntity;
import insty.model.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Entity
@Table(name = "course_progress", schema = "web_service")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CourseProgress extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // 수강자
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY) // 강좌
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "status")
    private CourseProgressStatus status;  // 수강 완료 상태 여부

    public void update(CourseProgressStatus status) {
        this.status = status;
    }

    public static CourseProgress create(User user,Course course,CourseProgressStatus status){
        validateCreate(user,course,status);
        return CourseProgress.builder()
                .user(user)
                .course(course)
                .status(status)
                .build();
    }

    private static void validateCreate(User user, Course course, CourseProgressStatus status) {
        if (user == null) {
            log.error("생성 오류 - user : null");
            throw new CustomException(CourseProgressErrorCode.COURSE_PROGRESS_CREATE_ERROR);
        }
        if (course == null) {
            log.error("생성 오류 - course : null");
            throw new CustomException(CourseProgressErrorCode.COURSE_PROGRESS_CREATE_ERROR);
        }
    }
}