package insty.model.courseqna;

import insty.model.course.Course;
import insty.model.user.User;

public class CommunityQuestionFixture {

    public static CourseQuestion getCommunityQuestion() {
        return CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser();
    }

    public static CourseQuestion getCommunityQuestion(Course course, User user) {
        return CourseQuestion.create(course, user, "질문 제목", "질문 내용", CommunityBoardType.QNA);
    }

    public static CourseQuestion getCommunityQuestion(Course course, User user, String title, String content) {
        return CourseQuestion.create(course, user, title, content, CommunityBoardType.QNA);
    }
}
