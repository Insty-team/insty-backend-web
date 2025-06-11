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
@Table(name = "course_install_env_checklists", schema = "web_service")
@Getter
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CourseInstallEnvChecklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private boolean isSupported;


    public static CourseInstallEnvChecklist create(Course course, String content, boolean isSupported) {
        validateCreate(course, content, isSupported);
        return CourseInstallEnvChecklist.builder()
                .course(course)
                .content(content)
                .isSupported(isSupported)
                .build();
    }

    private static void validateCreate(Course course, String content, boolean isSupported) {
        if (course == null) {
            log.error("CourseInstallEnvChecklist 생성 오류 - course : null");
            throw new CustomException(CourseErrorCode.COURSE_CREATE_ERROR);
        }
        if (content == null || content.trim().isEmpty()) {
            log.error("CourseInstallEnvChecklist 생성 오류 - content : 비었음");
            throw new CustomException(CourseErrorCode.COURSE_CREATE_ERROR);
        }
    }
}
