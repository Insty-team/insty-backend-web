package insty.domain.course.dto;

import insty.model.course.CourseRequest;
import insty.model.user.User;
import java.util.List;

public record CourseRequestRes(
        Long id,
        String title,
        String content
) {

    // 강의 생성했을 때
    public static CourseRequestRes from(CourseRequest courseRequest) {
        return new CourseRequestRes(
                courseRequest.getId(),
                courseRequest.getTitle(),
                courseRequest.getContent()
        );
    }

    // 나에게 강의 요청 리스트 조회
    public static List<CourseRequestRes> from(List<CourseRequest> courseRequests) {
        return courseRequests.stream()
                .map(CourseRequestRes::from)
                .toList();
    }
}