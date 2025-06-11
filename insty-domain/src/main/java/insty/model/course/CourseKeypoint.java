package insty.model.course;

import insty.error.CourseErrorCode;
import insty.exception.CustomException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "course_keypoints", schema = "web_service")
@Getter
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CourseKeypoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false, length = 500)
    private String content;


    public static CourseKeypoint create(Course course, String content) {
        validateCreate(course, content);
        return CourseKeypoint.builder()
                .course(course)
                .content(content)
                .build();
    }

    private static void validateCreate(Course course, String content) {
        if (course == null) {
            log.error("CourseKeypoint 생성 오류 - course : null");
            throw new CustomException(CourseErrorCode.COURSE_CREATE_ERROR);
        }
        if (content == null || content.trim().isEmpty()) {
            log.error("CourseKeypoint 생성 오류 - content : 비었음");
            throw new CustomException(CourseErrorCode.COURSE_CREATE_ERROR);
        }
    }
}
