package insty.model.course.fixture;

import insty.model.course.Course;

public class CourseFixtureBuilder {

    public static Course getCourse() {
        return Course.create("제목", "설명", 10000, "강의 추천 대상자", true);
    }
}
