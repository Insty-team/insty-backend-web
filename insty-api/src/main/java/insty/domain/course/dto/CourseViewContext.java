package insty.domain.course.dto;

public record CourseViewContext(
        Long userId
) {

    public static CourseViewContext of(Long userId) {
        return new CourseViewContext(userId);
    }
}

