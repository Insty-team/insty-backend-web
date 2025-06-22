package insty.domain.course.dto;

import insty.model.course.CourseRequest;
import insty.model.user.User;

public record CourseRequestRes(
        Long id,
        String title,
        String content,
        CreatorUser creatorUser
) {
    public static CourseRequestRes from(CourseRequest courseRequest, User creator) {
        return new CourseRequestRes(
                courseRequest.getId(),
                courseRequest.getTitle(),
                courseRequest.getContent(),
                CreatorUser.from(creator)  // 내부 레코드로 위임
        );
    }

    private record CreatorUser(
            Long creatorId,
            String nickname
    ) {
        private static CreatorUser from(User creator) {
            return new CreatorUser(
                    creator.getId(),
                    creator.getNickname()
            );
        }
    }
}