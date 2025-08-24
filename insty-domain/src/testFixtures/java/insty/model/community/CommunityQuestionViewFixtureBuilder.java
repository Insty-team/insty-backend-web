package testFixtures.java.insty.model.community;

import insty.model.community.CommunityQuestion;
import insty.model.community.CommunityQuestionView;
import insty.model.community.CommunityQuestionFixtureBuilder;
import insty.model.community.id.CommunityQuestionViewId;
import java.time.Instant;
import org.springframework.test.util.ReflectionTestUtils;

public class CommunityQuestionViewFixtureBuilder {

    public static CommunityQuestionView getCommunityQuestionViewWithId(Long questionId, Long userId) {
        CommunityQuestion question = CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser(questionId, "질문 제목", "질문 내용");
        CommunityQuestionView view = CommunityQuestionView.create(question, userId);
        return view;
    }

    public static CommunityQuestionView getCommunityQuestionViewWithIdAndLastViewedAt(Long questionId, Long userId, Instant lastViewedAt) {
        CommunityQuestionView view = getCommunityQuestionViewWithId(questionId, userId);
        ReflectionTestUtils.setField(view, "lastViewedAt", lastViewedAt);
        return view;
    }

    public static CommunityQuestionView getCommunityQuestionViewWithId(Long questionId, Long userId, String title, String content) {
        CommunityQuestion question = CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser(questionId, title, content);
        CommunityQuestionView view = CommunityQuestionView.create(question, userId);
        return view;
    }
}
