package insty.domain.community.implement.mail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import insty.global.property.AppProperties;
import insty.mail.MailContent;
import insty.mail.MailHelper;
import insty.mail.MailType;
import insty.model.community.CommunityAnswer;
import insty.model.community.CommunityQuestion;
import insty.model.community.CommunityQuestionFixtureBuilder;
import insty.model.user.User;
import insty.model.user.UserFixtureBuilder;
import java.util.Map;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class CommunityMailServiceTest {

    @InjectMocks
    private CommunityMailService communityMailService;

    @Mock
    private AppProperties appProperties;

    @Mock
    private MailHelper mailHelper;

    @Test
    void sendQuestionNotificationToCreator_정상_미리보기절삭_URL도메인적용() {
        // given
        Long questionId = 10L;
        String longContent = "가나다라마바사아자차카타파하"; // 길이 12
        String domain = "https://insty.co.kr";
        int previewLen = 10;

        CommunityQuestion question = CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser(
                questionId, "질문 제목", longContent);

        when(appProperties.getDomain()).thenReturn(domain);
        when(appProperties.getMailPreviewLength()).thenReturn(previewLen);

        ArgumentCaptor<MailContent> captor = ArgumentCaptor.forClass(MailContent.class);

        // when
        communityMailService.sendQuestionNotificationToCreator(question);

        // then
        verify(mailHelper).send(captor.capture());
        MailContent content = captor.getValue();
        assertThat(content).isInstanceOf(CommunityQuestionMailContent.class);
        assertThat(content.mailType()).isEqualTo(MailType.COMMUNITY_QUESTION);

        Map<String, Object> vars = content.variables();
        assertThat(vars.get("questionTitle")).isEqualTo("질문 제목");
        assertThat(vars.get("courseName")).isEqualTo(question.getCourse().getTitle());
        assertThat(vars.get("questionAuthorName")).isEqualTo(question.getUser().getNickname());
        assertThat(vars.get("questionContent")).isEqualTo(longContent.substring(0, previewLen) + "...");
        assertThat(vars.get("questionUrl")).isEqualTo(domain + "/community/questions/" + questionId);
        assertThat(content.to()).isEqualTo(question.getCourse().getUser().getEmail());
    }

    @Test
    void sendAnswerNotification_정상_답변자가_Creator인_경우_수신자는_Runner() {
        // given
        Long questionId = 20L;
        String questionTitle = "제목";
        String answerText = "답변 내용";
        int previewLen = 100;
        String domain = "https://insty.co.kr";

        CommunityQuestion question = CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser(
                questionId, questionTitle, "내용");
        // Creator의 id는 fixture 기본값 1L
        CommunityAnswer answerByCreator = CommunityAnswer.create(
                question,
                UserFixtureBuilder.getUserWithId(1L),
                answerText
        );

        when(appProperties.getDomain()).thenReturn(domain);
        when(appProperties.getMailPreviewLength()).thenReturn(previewLen);

        ArgumentCaptor<MailContent> captor = ArgumentCaptor.forClass(MailContent.class);

        // when
        communityMailService.sendAnswerNotification(question, answerByCreator);

        // then
        verify(mailHelper).send(captor.capture());
        MailContent mail = captor.getValue();
        assertThat(mail).isInstanceOf(CommunityAnswerMailContent.class);
        assertThat(mail.mailType()).isEqualTo(MailType.COMMUNITY_ANSWER);

        Map<String, Object> vars = mail.variables();
        assertThat(vars.get("questionTitle")).isEqualTo(questionTitle);
        assertThat(vars.get("answerContent")).isEqualTo(answerText);
        assertThat(vars.get("answerAuthorName")).isEqualTo(answerByCreator.getUser().getNickname());
        assertThat(vars.get("questionUrl")).isEqualTo(domain + "/community/questions/" + questionId);
        // Creator가 답변 → Runner에게 전송
        assertThat(mail.to()).isEqualTo(question.getUser().getEmail());
    }

    @Test
    void sendAnswerNotification_정상_답변자가_Runner인_경우_수신자는_Creator() {
        // given
        Long questionId = 30L;
        String questionTitle = "제목2";
        String answerText = "다른 답변";
        int previewLen = 100;
        String domain = "https://insty.co.kr";

        CommunityQuestion question = CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser(
                questionId, questionTitle, "내용");
        // Runner id로 설정 (Creator id 1L와 다르게 2L)
        User runnerUser = UserFixtureBuilder.getUserWithId(2L);
        CommunityAnswer answerByRunner = CommunityAnswer.create(question, runnerUser, answerText);

        when(appProperties.getDomain()).thenReturn(domain);
        when(appProperties.getMailPreviewLength()).thenReturn(previewLen);

        ArgumentCaptor<MailContent> captor = ArgumentCaptor.forClass(MailContent.class);

        // when
        communityMailService.sendAnswerNotification(question, answerByRunner);

        // then
        verify(mailHelper).send(captor.capture());
        MailContent mail = captor.getValue();
        assertThat(mail).isInstanceOf(CommunityAnswerMailContent.class);
        assertThat(mail.mailType()).isEqualTo(MailType.COMMUNITY_ANSWER);

        Map<String, Object> vars = mail.variables();
        assertThat(vars.get("questionTitle")).isEqualTo(questionTitle);
        assertThat(vars.get("answerContent")).isEqualTo(answerText);
        assertThat(vars.get("answerAuthorName")).isEqualTo(runnerUser.getNickname());
        assertThat(vars.get("questionUrl")).isEqualTo(domain + "/community/questions/" + questionId);
        // Runner가 답변 → Creator에게 전송
        assertThat(mail.to()).isEqualTo(question.getCourse().getUser().getEmail());
    }
}


