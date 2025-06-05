package insty.domain.community.service;

import insty.domain.community.dto.CommunityQuestionReq;
import insty.domain.community.dto.CommunityQuestionRes;
import insty.domain.community.implement.CommunityReader;
import insty.domain.community.implement.CommunityWriter;
import insty.domain.course.implement.CourseReader;
import insty.domain.user.implement.UserReader;
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

    @Test
    void getQuestion() {

    }

    @Test
    void saveQuestion(){
        String title = "제목";
        String content = "내용";
        Long userId = 1L;
        Long courseId = 1L;

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
        when(communityWriter.saveQuestion(any(CommunityQuestionReq.class), any(Course.class), any(User.class)))
                .thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));


        //when
        CommunityQuestionRes communityQuestionRes = communityService.saveQuestion(communityQuestionReq);
        //then
        assertThat(communityQuestionRes).isNotNull();
        assertThat(communityQuestionRes.title()).isEqualTo(res.title());
        assertThat(communityQuestionRes.content()).isEqualTo(res.content());

    }
}
