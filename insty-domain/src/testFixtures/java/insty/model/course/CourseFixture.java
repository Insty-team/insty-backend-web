package insty.model.course;

import insty.model.user.User;

public class CourseFixture {

    public static Course getCourse(User user) {
        return Course.create(user, "제목", "설명", 10000, "강의 추천 대상자", true);
    }
}
