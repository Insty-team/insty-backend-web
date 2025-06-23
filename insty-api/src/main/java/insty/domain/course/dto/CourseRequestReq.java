package insty.domain.course.dto;

public record CourseRequestReq (
    String title,
    String content,
    Long creatorId
) {

}
