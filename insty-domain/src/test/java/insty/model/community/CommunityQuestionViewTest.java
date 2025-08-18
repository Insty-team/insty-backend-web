package insty.model.community;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import insty.error.CommunityErrorCode;
import insty.exception.CustomException;
import insty.model.course.Course;
import insty.model.course.CourseFixtureBuilder;
import insty.model.user.User;
import insty.model.user.UserFixtureBuilder;
import java.time.Instant;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class CommunityQuestionViewTest {

    @Test
    void create_정상() {
        // given
        CommunityQuestion communityQuestion = CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser();
        Long userId = 100L;

        // when
        CommunityQuestionView view = CommunityQuestionView.create(communityQuestion, userId);

        // then
        assertThat(view).isNotNull();
        assertThat(view.getCommunityQuestionViewId()).isNotNull();
        assertThat(view.getCommunityQuestionViewId().getCommunityQuestion()).isEqualTo(communityQuestion.getId());
        assertThat(view.getCommunityQuestionViewId().getUserId()).isEqualTo(userId);
        assertThat(view.getCommunityQuestion()).isEqualTo(communityQuestion);
        assertThat(view.getLastViewedAt()).isNotNull();
        assertThat(view.getLastViewedAt()).isBeforeOrEqualTo(Instant.now());
    }

    @Test
    void create_에러_communityQuestion이_null이다() {
        // given
        CommunityQuestion communityQuestion = null;
        Long userId = 100L;

        // when & then
        assertThatThrownBy(() -> CommunityQuestionView.create(communityQuestion, userId))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
    }

    @Test
    void create_에러_userId가_null이다() {
        // given
        CommunityQuestion communityQuestion = CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser();
        Long userId = null;

        // when & then
        assertThatThrownBy(() -> CommunityQuestionView.create(communityQuestion, userId))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
    }

    @Test
    void updateLastViewedAt_정상() throws InterruptedException {
        // given
        CommunityQuestion communityQuestion = CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser();
        Long userId = 100L;
        
        CommunityQuestionView view = CommunityQuestionView.create(communityQuestion, userId);
        Instant originalTime = view.getLastViewedAt();
        
        Thread.sleep(100); // 시간 차이를 위해 잠시 대기

        // when
        view.updateLastViewedAt();

        // then
        assertThat(view.getLastViewedAt()).isAfter(originalTime);
        assertThat(view.getLastViewedAt()).isBeforeOrEqualTo(Instant.now());
    }

    @Test
    void create_복합키_검증() {
        // given
        CommunityQuestion communityQuestion = CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser();
        Long userId = 200L;

        // when
        CommunityQuestionView view = CommunityQuestionView.create(communityQuestion, userId);

        // then
        assertThat(view.getCommunityQuestionViewId()).isNotNull();
        assertThat(view.getCommunityQuestionViewId().getCommunityQuestion()).isEqualTo(communityQuestion.getId());
        assertThat(view.getCommunityQuestionViewId().getUserId()).isEqualTo(userId);
        
        // 복합키의 구성 요소들이 올바르게 설정되었는지 검증
        assertThat(view.getCommunityQuestion().getId()).isEqualTo(communityQuestion.getId());
        assertThat(view.getCommunityQuestionViewId().getUserId()).isEqualTo(userId);
    }

    @Test
    void create_엔티티_관계_검증() {
        // given
        CommunityQuestion communityQuestion = CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser();
        Long userId = 300L;

        // when
        CommunityQuestionView view = CommunityQuestionView.create(communityQuestion, userId);

        // then
        assertThat(view.getCommunityQuestion()).isNotNull();
        assertThat(view.getCommunityQuestion()).isEqualTo(communityQuestion);
        assertThat(view.getCommunityQuestion().getId()).isEqualTo(communityQuestion.getId());
        assertThat(view.getCommunityQuestion().getTitle()).isEqualTo(communityQuestion.getTitle());
        assertThat(view.getCommunityQuestion().getContent()).isEqualTo(communityQuestion.getContent());
    }
}
