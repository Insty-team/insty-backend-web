package insty.domain.community.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
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
import insty.domain.file.implement.FileWriter;
import insty.domain.user.implement.UserReader;
import insty.global.property.AppProperties;
import insty.model.community.CommunityAnswer;
import insty.model.community.CommunityAnswerFile;
import insty.model.community.CommunityFile;
import insty.model.community.CommunityQuestion;
import insty.model.course.Course;
import insty.model.course.CourseFixtureBuilder;
import insty.model.file.File;
import insty.model.file.FileFixtureBuilder;
import insty.model.user.User;
import insty.model.user.UserFixtureBuilder;
import insty.s3.adapter.S3FileManager;
import insty.s3.adapter.S3UrlIssuer;
import java.lang.reflect.Field;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
public class CommunityServiceTest {

    @InjectMocks
    CommunityService communityService;

    @Mock
    CommunityWriter communityWriter;
    @Mock
    CommunityReader communityReader;
    @Mock
    CourseReader courseReader;
    @Mock
    UserReader userReader;
    @Mock
    FileWriter fileWriter;
    @Mock
    AppProperties appProperties;
    @Mock
    S3FileManager s3FileManager;
    @Mock
    S3UrlIssuer s3UrlIssuer;
    @Mock
    CloudFrontSigner cloudFrontSigner;

    @Mock
    CommunityQuestionRepository communityQuestionRepository;
    @Mock
    CommunityAnswerRepository communityAnswerRepository;

    // ID를 설정하는 헬퍼 메서드
    private void setId(CommunityAnswer answer, Long id) {
        try {
            Field idField = CommunityAnswer.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(answer, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set ID", e);
        }
    }

    private void setId(CommunityQuestion question, Long id) {
        try {
            Field idField = CommunityQuestion.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(question, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set ID", e);
        }
    }

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
        lenient().when(communityReader.getCommunityAnswerFilesByAnswerId(anyString()))
                .thenReturn(List.of());
        lenient().when(appProperties.getDomain())
                .thenReturn("test.com");

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
        lenient().when(appProperties.getDomain())
                .thenReturn("test.com");

        //when
        CommunityQuestionRes communityQuestionRes = communityService.saveQuestion(communityQuestionReq, null);
        //then
        assertThat(communityQuestionRes).isNotNull();
        assertThat(communityQuestionRes.title()).isEqualTo(title);
        assertThat(communityQuestionRes.content()).isEqualTo(content);
    }

    @Test
    void saveQuestionWithFiles() {
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

        User user = UserFixtureBuilder.getUserWithId();
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();

        CommunityQuestion communityQuestion = CommunityQuestion.create(
                course,
                user,
                title,
                content
        );
        setId(communityQuestion, 1L);

        File file = FileFixtureBuilder.getCourseThumbnailWithId();
        CommunityFile communityFile = CommunityFile.create(communityQuestion, file);

        when(courseReader.getCourseById(courseId))
                .thenReturn(course);
        when(userReader.getUser(userId))
                .thenReturn(user);
        when(communityWriter.saveQuestion(any(CommunityQuestion.class), any(Course.class), any(User.class)))
                .thenReturn(communityQuestion);
        when(fileWriter.saveFiles(any()))
                .thenReturn(List.of(file));
        when(communityWriter.saveCommunityFiles(any()))
                .thenReturn(List.of(communityFile));
        lenient().when(appProperties.getDomain())
                .thenReturn("test.com");

        //when
        CommunityQuestionRes communityQuestionRes = communityService.saveQuestion(communityQuestionReq, attachments);
        //then
        assertThat(communityQuestionRes).isNotNull();
        assertThat(communityQuestionRes.title()).isEqualTo(title);
        assertThat(communityQuestionRes.content()).isEqualTo(content);
        assertThat(communityQuestionRes.attachments()).isNotNull();
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
        setId(communityAnswer1, 1L);
        
        CommunityAnswer communityAnswer2 = CommunityAnswer.create(
                communityQuestion,
                user,
                content2
        );
        setId(communityAnswer2, 2L);
        
        CommunityAnswer communityAnswer3 = CommunityAnswer.create(
                communityQuestion,
                user,
                content3
        );
        setId(communityAnswer3, 3L);

        when(communityReader.getAllCommunityAnswers(questionId))
                .thenReturn(List.of(communityAnswer1, communityAnswer2, communityAnswer3));
        lenient().when(communityReader.getCommunityAnswerFilesByAnswerId(anyString()))
                .thenReturn(List.of());
        lenient().when(appProperties.getDomain())
                .thenReturn("test.com");

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
        setId(communityAnswer, 1L);

        when(communityReader.getCommunityQuestionDetailsById(questionId))
                .thenReturn(communityQuestion);
        when(userReader.getUser(userId))
                .thenReturn(user);
        when(communityWriter.saveAnswer(any(CommunityQuestion.class), any(), any(User.class)))
                .thenReturn(communityAnswer);
        lenient().when(communityReader.getCommunityAnswerFilesByAnswerId(anyString()))
                .thenReturn(List.of());
        lenient().when(appProperties.getDomain())
                .thenReturn("test.com");

        //when
        CommunityAnswerRes communityAnswerRes = communityService.saveAnswer(req, null, null);

        //then
        assertThat(communityAnswerRes).isNotNull();
        assertThat(communityAnswerRes.content()).isEqualTo(content);
        assertThat(communityAnswerRes.attachments()).isNotNull();
    }

    @Test
    void saveAnswerWithFiles() {
        String questionId = "1";
        String content = "답변 내용";
        Long userId = 1L;

        CommunityAnswerReq req = CommunityAnswerReq.create(
                questionId,
                userId,
                content
        );

        List<MultipartFile> imageFiles = List.of(
                new MockMultipartFile("imageFile", "image1.jpg", "image/jpeg", "내용".getBytes()));

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
        setId(communityAnswer, 1L);

        File file = FileFixtureBuilder.getCourseThumbnailWithId();
        CommunityAnswerFile communityAnswerFile = CommunityAnswerFile.create(communityAnswer, file);

        when(communityReader.getCommunityQuestionDetailsById(questionId))
                .thenReturn(communityQuestion);
        when(userReader.getUser(userId))
                .thenReturn(user);
        when(communityWriter.saveAnswer(any(CommunityQuestion.class), any(), any(User.class)))
                .thenReturn(communityAnswer);
        when(fileWriter.saveFiles(any()))
                .thenReturn(List.of(file));
        when(communityWriter.saveCommunityAnswerFiles(any()))
                .thenReturn(List.of(communityAnswerFile));
        when(communityReader.getCommunityAnswerFilesByAnswerId(anyString()))
                .thenReturn(List.of(communityAnswerFile));
        lenient().when(appProperties.getDomain())
                .thenReturn("test.com");

        //when
        CommunityAnswerRes communityAnswerRes = communityService.saveAnswer(req, imageFiles, null);

        //then
        assertThat(communityAnswerRes).isNotNull();
        assertThat(communityAnswerRes.content()).isEqualTo(content);
        assertThat(communityAnswerRes.attachments()).isNotNull();
        assertThat(communityAnswerRes.attachments().size()).isEqualTo(1);
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

        when(communityReader.getCommunityAnswerById(String.valueOf(answerId)))
                .thenReturn(communityAnswer);

        //when
        communityService.deleteAnswer(String.valueOf(answerId));

        //then
        // deleteAnswer는 void 메서드이므로 예외가 발생하지 않으면 성공
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
        setId(communityAnswer, 1L);

        CommunityAnswer updatedCommunityAnswer = CommunityAnswer.create(
                communityQuestion,
                user,
                content
        );
        setId(updatedCommunityAnswer, 1L);

        CommunityAnswerReq req = new CommunityAnswerReq(
                "1",
                String.valueOf(communityQuestion.getId()),
                1L,
                content,
                null
        );

        when(communityReader.getCommunityAnswerById(answerId))
                .thenReturn(communityAnswer);
        when(communityWriter.updateAnswer(any(CommunityAnswer.class), any()))
                .thenReturn(updatedCommunityAnswer);
        lenient().when(communityReader.getCommunityAnswerFilesByAnswerId(answerId))
                .thenReturn(List.of());
        lenient().when(appProperties.getDomain())
                .thenReturn("test.com");

        //when
        CommunityAnswerRes communityAnswerRes = communityService.updateAnswer(req, null, null);

        //then
        assertThat(communityAnswerRes).isNotNull();
        assertThat(communityAnswerRes.content()).isEqualTo(content);
        assertThat(communityAnswerRes.attachments()).isNotNull();
    }

    @Test
    void updateAnswerWithFiles() {
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
        setId(communityAnswer, 1L);

        CommunityAnswer updatedCommunityAnswer = CommunityAnswer.create(
                communityQuestion,
                user,
                content
        );
        setId(updatedCommunityAnswer, 1L);

        CommunityAnswerReq req = new CommunityAnswerReq(
                "1",
                String.valueOf(communityQuestion.getId()),
                1L,
                content,
                null
        );

        List<MultipartFile> imageFiles = List.of(
                new MockMultipartFile("imageFile", "image1.jpg", "image/jpeg", "내용".getBytes()));

        File file = FileFixtureBuilder.getCourseThumbnailWithId();
        CommunityAnswerFile communityAnswerFile = CommunityAnswerFile.create(updatedCommunityAnswer, file);

        when(communityReader.getCommunityAnswerById(answerId))
                .thenReturn(communityAnswer);
        when(communityWriter.updateAnswer(any(CommunityAnswer.class), any()))
                .thenReturn(updatedCommunityAnswer);
        when(fileWriter.saveFiles(any()))
                .thenReturn(List.of(file));
        when(communityWriter.saveCommunityAnswerFiles(any()))
                .thenReturn(List.of(communityAnswerFile));
        when(communityReader.getCommunityAnswerFilesByAnswerId(answerId))
                .thenReturn(List.of(communityAnswerFile));
        lenient().when(appProperties.getDomain())
                .thenReturn("test.com");

        //when
        CommunityAnswerRes communityAnswerRes = communityService.updateAnswer(req, imageFiles, null);

        //then
        assertThat(communityAnswerRes).isNotNull();
        assertThat(communityAnswerRes.content()).isEqualTo(content);
        assertThat(communityAnswerRes.attachments()).isNotNull();
        assertThat(communityAnswerRes.attachments().size()).isEqualTo(1);
    }
}



