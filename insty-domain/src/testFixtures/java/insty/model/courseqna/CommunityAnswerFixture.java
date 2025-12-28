package insty.model.courseqna;

import insty.model.user.User;

public class CommunityAnswerFixture {

    public static CourseAnswer getCommunityAnswer(CourseQuestion courseQuestion, User user) {
        return CourseAnswer.create(courseQuestion, user, "답변 내용");
    }

    public static CourseAnswer getCommunityAnswer(CourseQuestion courseQuestion, User user, String content) {
        return CourseAnswer.create(courseQuestion, user, content);
    }
}
