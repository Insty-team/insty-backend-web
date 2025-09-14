package insty.domain.course.dto;

import insty.model.course.CourseProgress;
import insty.model.course.CourseProgressStatus;

public record CourseProgressRes (
        Long userId,
        Long courseId,
        CourseProgressStatus status
){

    public static CourseProgressRes from(CourseProgress courseProgress){
        return new CourseProgressRes(courseProgress.getUser().getId(),
                                    courseProgress.getCourse().getId(),
                                    courseProgress.getStatus());
    }
}
