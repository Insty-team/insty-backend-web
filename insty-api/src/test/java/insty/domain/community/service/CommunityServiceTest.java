package insty.domain.community.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import insty.cloudfront.adapter.CloudFrontSigner;
import insty.domain.community.dto.CommunityAnswerReq;
import insty.domain.community.dto.CommunityAnswerRes;
import insty.domain.community.dto.CommunityQuestionReq;
import insty.domain.community.dto.CommunityQuestionRes;
import insty.domain.community.implement.CommunityReader;
import insty.domain.community.implement.CommunityWriter;
import insty.domain.community.reposiotry.CommunityAnswerRepository;
import insty.domain.community.reposiotry.CommunityQuestionRepository;
import insty.domain.course.implement.CourseReader;
import insty.domain.user.implement.UserReader;
import insty.global.property.AppProperties;
import insty.model.community.CommunityAnswer;
import insty.model.community.CommunityFile;
import insty.model.community.CommunityQuestion;
import insty.model.course.Course;
import insty.model.course.CourseFixtureBuilder;
import insty.model.file.File;
import insty.model.file.FileFixtureBuilder;
import insty.model.user.User;
import insty.model.user.UserFixtureBuilder;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import insty.s3.adapter.S3FileManager;
import insty.s3.adapter.S3UrlIssuer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    @Mock
    CommunityAnswerRepository communityAnswerRepository;

    @Mock
    S3UrlIssuer s3UrlIssuer;
    @Mock
    S3FileManager s3FileManager;
    @Mock
    CloudFrontSigner cloudFrontSigner;
    @Mock
    AppProperties appProperties;

    @Test
    void getQuestionDetails() {
        String questionId = "1";
        String title = "제목";
        String content = "내용";

        User user = UserFixtureBuilder.getUserWithId();
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();

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
    void create_question_정상() {
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
                null,
                null
        );

        User user = UserFixtureBuilder.getUserWithId();
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();

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
        List<MultipartFile> attachments = List.of(
                new MockMultipartFile("practiceFile", "practice1.jpg", "image/jpeg", "내용".getBytes()));

        File file = FileFixtureBuilder.getCourseThumbnailWithId();

        CommunityQuestionRes res = CommunityQuestionRes.create(
                userId,
                courseId,
                title,
                content,
                Instant.now(),
                Instant.now(),
                null,
                null
        );

        User user = UserFixtureBuilder.getUserWithId();
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();

        CommunityQuestion communityQuestion = CommunityQuestion.create(
                course,
                user,
                title,
                content
        );

//        when(courseReader.getCourseById(courseId))
//                .thenReturn(course);
//        when(userReader.getUser(userId))
//                .thenReturn(user);
//        when(communityWriter.saveQuestion(any(CommunityQuestion.class), any(Course.class), any(User.class)))
//                .thenReturn(communityQuestion);
//        when(communityWriter.saveCommunityFiles(any()))
//                .thenReturn(List.of(CommunityFile.create(communityQuestion, file)));


    }

    @Test
    void getAllAnswers() {
        String questionId = "1";

        User user = UserFixtureBuilder.getUserWithId();
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();

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
                content,
                Instant.now(),
                Instant.now()
        );

        User user = UserFixtureBuilder.getUserWithId();
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();

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
        CommunityAnswerRes communityAnswerRes = communityService.saveAnswer(req, null);

        //then
        assertThat(communityAnswerRes).isNotNull();
        assertThat(communityAnswerRes.content()).isEqualTo(content);
    }

    @Test
    void deleteAnswer() {
        Long answerId = 1L;

        User user = UserFixtureBuilder.getUserWithId();
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();

        CommunityQuestion communityQuestion = CommunityQuestion.create(
                course,
                user,
                "질문 제목",
                "질문 내용"
        );

        CommunityAnswer communityAnswer = CommunityAnswer.create(
                communityQuestion,
                user,
                "답변 내용"
        );

        CommunityAnswerReq communityAnswerReq = CommunityAnswerReq.create(
                String.valueOf(communityQuestion.getId()),
                user.getId(),
                communityAnswer.getContent()
        );

//        when(communityReader.getCommunityAnswerById(any(String.class)))
//                .thenReturn(communityAnswer);

        //when
        communityService.deleteAnswer(communityAnswerReq);

        //then
        Optional<CommunityAnswer> deletedAnswer = communityAnswerRepository.findById(answerId);
        assertThat(deletedAnswer.isPresent()).isFalse();
    }

    @Test
    void updateAnswer() {
        String answerId = "1";
        String content = "수정된 답변 내용";



        User user = UserFixtureBuilder.getUserWithId();
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();

        CommunityQuestion communityQuestion = CommunityQuestion.create(
                course,
                user,
                "질문 제목",
                "질문 내용"
        );

        CommunityAnswer communityAnswer = CommunityAnswer.create(
                communityQuestion,
                user,
                "기존 답변 내용"
        );

        CommunityAnswer updatedCommunityAnswer = CommunityAnswer.create(
                communityQuestion,
                user,
                content
        );

        CommunityAnswerReq req = new CommunityAnswerReq(
                "1",
                String.valueOf(communityQuestion.getId()),
                1L,
                content
        );



        when(communityReader.getCommunityAnswerById(answerId))
                .thenReturn(communityAnswer);
        when(communityWriter.updateAnswer(any(CommunityAnswer.class), any()))
                .thenReturn(updatedCommunityAnswer);

        //when
        CommunityAnswerRes communityAnswerRes = communityService.updateAnswer(req);

        //then
        assertThat(communityAnswerRes).isNotNull();
        assertThat(communityAnswerRes.content()).isEqualTo(content);
    }

}



