package insty.domain.course.dto;


import insty.model.course.Course;

public record CoursePatchVisibleRes(
        Long courseId,
        String title,
        Boolean isShow
){
    public static CoursePatchVisibleRes from(Course course){
        return new CoursePatchVisibleRes(course.getId(),
                                         course.getTitle(),
                                         course.isShow()
                                         );
    }
}
