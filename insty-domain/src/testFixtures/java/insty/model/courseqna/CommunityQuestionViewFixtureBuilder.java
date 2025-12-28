package testFixtures.java.insty.model.course;

import insty.model.courseqna.CommunityQuestionFixtureBuilder;
import insty.model.courseqna.CourseQuestion;
import insty.model.courseqna.CourseQuestionView;
import java.time.Instant;
import org.springframework.test.util.ReflectionTestUtils;

public class CommunityQuestionViewFixtureBuilder {

    public static CourseQuestionView getCommunityQuestionViewWithId(Long questionId, Long userId) {
        CourseQuestion question = CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser(questionId, "질문 제목", "질문 내용");
        CourseQuestionView view = CourseQuestionView.create(question, userId);
        return view;
    }

    public static CourseQuestionView getCommunityQuestionViewWithIdAndLastViewedAt(Long questionId, Long userId, Instant lastViewedAt) {
        CourseQuestionView view = getCommunityQuestionViewWithId(questionId, userId);
        ReflectionTestUtils.setField(view, "lastViewedAt", lastViewedAt);
        return view;
    }

    public static CourseQuestionView getCommunityQuestionViewWithId(Long questionId, Long userId, String title, String content) {
        CourseQuestion question = CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser(questionId, title, content);
        CourseQuestionView view = CourseQuestionView.create(question, userId);
        return view;
    }
}
