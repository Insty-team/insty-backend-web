package insty.domain.community.service;

import insty.domain.community.dto.CommunityQuestionReq;
import insty.domain.community.dto.CommunityQuestionRes;
import insty.domain.community.implement.CommunityReader;
import insty.domain.community.implement.CommunityWriter;
import insty.domain.community.reposiotry.CommunityQuestionRepository;
import insty.domain.course.implement.CourseReader;
import insty.domain.user.implement.UserReader;
import insty.model.community.CommunityQuestion;
import insty.model.course.Course;
import insty.model.user.User;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

        when(userReader.getUser(nullable(Long.class)))
                .thenReturn(user);

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
                courseId,
                userId,
                title,
                content
        );

        CommunityQuestionRes res = CommunityQuestionRes.create(
                title,
                content
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
        when(communityWriter.saveQuestion(communityQuestionReq, course, user))
                .thenReturn(communityQuestion);

        //when
        CommunityQuestionRes communityQuestionRes = communityService.saveQuestion(communityQuestionReq);
        //then
        assertThat(communityQuestionRes).isNotNull();
        assertThat(communityQuestionRes.title()).isEqualTo(title);
        assertThat(communityQuestionRes.content()).isEqualTo(content);

    }
}
