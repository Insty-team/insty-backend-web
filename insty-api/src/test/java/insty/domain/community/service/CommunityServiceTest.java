package insty.domain.community.service;

import insty.domain.community.dto.CommunityAnswerReq;
import insty.domain.community.dto.CommunityAnswerRes;
import insty.domain.community.dto.CommunityQuestionReq;
import insty.domain.community.dto.CommunityQuestionRes;
import insty.domain.community.implement.CommunityReader;
import insty.domain.community.implement.CommunityWriter;
import insty.domain.community.reposiotry.CommunityQuestionRepository;
import insty.domain.course.implement.CourseReader;
import insty.domain.user.implement.UserReader;
import insty.model.community.CommunityAnswer;
import insty.model.community.CommunityQuestion;
import insty.model.course.Course;
import insty.model.user.User;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
public class CommunityServiceTest {


    @InjectMocks
    CommunityServiceImpl communityService;

    @Mock
    CommunityWriter communityWriter;
    @Mock
    CommunityReader communityReader;
    @Mock
    CourseReader courseReader;
    @Mock
    UserReader userReader;

    @Mock
    CommunityQuestionRepository communityQuestionRepository;

    @Test
    void getQuestionDetails() {
        String questionId = "1";
        String title = "제목";
        String content = "내용";

        User user = User.create("email", "nickname", "password");
        Course course = Course.create("title", "description", 100, "targetAudience", true);

        CommunityQuestion communityQuestion = CommunityQuestion.create(
                course,
                user,
                title,
                content
        );

        when(communityReader.getCommunityQuestionDetailsById(questionId))
                .thenReturn(communityQuestion);


        //when
        CommunityQuestionRes communityQuestionRes = communityService.getQuestionDetails(questionId);
        //then
        assertThat(communityQuestionRes).isNotNull();
        assertThat(communityQuestionRes.title()).isEqualTo(title);
        assertThat(communityQuestionRes.content()).isEqualTo(content);

    }

    @Test
    void saveQuestion(){
        String title = "제목";
        String content = "내용";
        Long userId = 1L;
        Long courseId = 2L;

        CommunityQuestionReq communityQuestionReq = CommunityQuestionReq.create(
                null,
                courseId,
                userId,
                title,
                content
        );

        CommunityQuestionRes res = CommunityQuestionRes.create(
                userId,
                courseId,
                title,
                content,
                Instant.now(),
                Instant.now(),
                null
        );

        User user = User.create("email", "nickname", "password");
        Course course = Course.create("title", "description", 100, "targetAudience", true);

        CommunityQuestion communityQuestion = CommunityQuestion.create(
                course,
                user,
                title,
                content
        );

        when(courseReader.getCourseById(courseId))
                .thenReturn(course);
        when(userReader.getUser(userId))
                .thenReturn(user);
        when(communityWriter.saveQuestion(any(CommunityQuestion.class), any(Course.class), any(User.class)))
                .thenReturn(communityQuestion);

        //when
        CommunityQuestionRes communityQuestionRes = communityService.saveQuestion(communityQuestionReq, null);
        //then
        assertThat(communityQuestionRes).isNotNull();
        assertThat(communityQuestionRes.title()).isEqualTo(title);
        assertThat(communityQuestionRes.content()).isEqualTo(content);

    }

    @Test
    void saveQustionWithFiles() {

    }

    @Test
    void getAllAnswers() {
        String questionId = "1";

        User user = User.create("email", "nickname", "password");
        Course course = Course.create("title", "description", 100, "targetAudience", true);

        CommunityQuestion communityQuestion = CommunityQuestion.create(
                course,
                user,
                "질문 제목",
                "질문 내용"
        );

        String content1 = "답변 내용1";
        String content2 = "답변 내용2";
        String content3 = "답변 내용3";


        CommunityAnswer communityAnswer1 = CommunityAnswer.create(
                communityQuestion,
                user,
                content1
        );
        CommunityAnswer communityAnswer2 = CommunityAnswer.create(
                communityQuestion,
                user,
                content2
        );
        CommunityAnswer communityAnswer3 = CommunityAnswer.create(
                communityQuestion,
                user,
                content3
        );

        when(communityReader.getAllCommunityAnswers(questionId))
                .thenReturn(List.of(communityAnswer1, communityAnswer2, communityAnswer3));

        //when
        List<CommunityAnswerRes> communityAnswerResList = communityService.getAllAnswers(questionId);

        //then
        assertThat(communityAnswerResList).isNotNull();
        assertThat(communityAnswerResList.size()).isEqualTo(3);
        assertThat(communityAnswerResList.get(0).content()).isEqualTo(content1);
        assertThat(communityAnswerResList.get(1).content()).isEqualTo(content2);
        assertThat(communityAnswerResList.get(2).content()).isEqualTo(content3);


    }

    @Test
    void saveAnswer() {
        String questionId = "1";
        String content = "답변 내용";
        Long userId = 1L;

        CommunityAnswerReq req = CommunityAnswerReq.create(
                questionId,
                userId,
                content
        );

        CommunityAnswerRes res = CommunityAnswerRes.create(
                userId,
                questionId,
                content,
                Instant.now(),
                Instant.now()
        );

        User user = User.create("email", "nickname", "password");
        Course course = Course.create("title", "description", 100, "targetAudience", true);

        CommunityQuestion communityQuestion = CommunityQuestion.create(
                course,
                user,
                "질문 제목",
                "질문 내용"
        );

        CommunityAnswer communityAnswer = CommunityAnswer.create(
                communityQuestion,
                user,
                content
        );

        when(communityReader.getCommunityQuestionDetailsById(questionId))
                .thenReturn(communityQuestion);
        when(userReader.getUser(userId))
                .thenReturn(user);
        when(communityWriter.saveAnswer(any(CommunityQuestion.class), any(), any(User.class)))
                .thenReturn(communityAnswer);

        //when
        CommunityAnswerRes communityAnswerRes = communityService.saveAnswer(req);

        //then
        assertThat(communityAnswerRes).isNotNull();
        assertThat(communityAnswerRes.content()).isEqualTo(content);
    }
}

