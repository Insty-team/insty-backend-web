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
import insty.domain.community.dto.CommunityQuestionSearchReq;
import insty.domain.common.SearchRes;
import insty.domain.common.dto.PaginationRes;
import insty.domain.community.implement.CommunityReader;
import insty.domain.community.implement.CommunityValidator;
import insty.domain.community.implement.CommunityWriter;
import insty.domain.community.implement.CommunityComplexReader;
import insty.domain.community.repository.CommunityAnswerRepository;
import insty.domain.community.repository.CommunityQuestionRepository;
import insty.domain.course.implement.CourseReader;
import insty.domain.file.implement.FileWriter;
import insty.domain.user.implement.UserReader;
import insty.global.property.AppProperties;
import insty.model.community.CommunityAnswer;
import insty.model.community.CommunityAnswerFile;
import insty.model.community.CommunityFile;
import insty.model.community.CommunityQuestion;
import insty.model.community.CommunityQuestionFixtureBuilder;
import insty.model.course.Course;
import insty.model.course.CourseFixtureBuilder;
import insty.model.file.File;
import insty.model.file.FileFixtureBuilder;
import insty.model.user.User;
import insty.model.user.UserFixtureBuilder;
import insty.s3.adapter.S3FileManager;
import insty.s3.adapter.S3UrlIssuer;
import insty.domain.community.implement.CommunityFileManager;
import insty.domain.common.FileInfo;
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
    CommunityValidator communityValidator;
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
    CommunityFileManager communityFileManager;

    @Mock
    CommunityQuestionRepository communityQuestionRepository;
    @Mock
    CommunityAnswerRepository communityAnswerRepository;
    @Mock
    CommunityComplexReader communityComplexReader;

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
    void getQuestionDetails_정상() {
        // given
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
    void saveQuestion_정상() { // create_question_정상
        // given
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
        when(communityFileManager.saveQuestionFiles(any(), any()))
                .thenReturn(List.of());
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
    void saveQuestion_파일첨부_정상() {
        // given
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
        FileInfo fileInfo = FileInfo.from(file, "test.com");

        when(courseReader.getCourseById(courseId))
                .thenReturn(course);
        when(userReader.getUser(userId))
                .thenReturn(user);
        when(communityWriter.saveQuestion(any(CommunityQuestion.class), any(Course.class), any(User.class)))
                .thenReturn(communityQuestion);
        when(communityFileManager.saveQuestionFiles(any(), any()))
                .thenReturn(List.of(fileInfo));
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
    void getAllAnswers_정상() {
        // given
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

        when(communityReader.getAllCommunityAnswers(questionId)).thenReturn(List.of(communityAnswer1, communityAnswer2, communityAnswer3));
        when(communityReader.getCommunityAnswerById("1")).thenReturn(communityAnswer1);
        when(communityReader.getCommunityAnswerById("2")).thenReturn(communityAnswer2);
        when(communityReader.getCommunityAnswerById("3")).thenReturn(communityAnswer3);
        when(communityReader.getCommunityAnswerFilesByAnswerId(anyString())).thenReturn(List.of());
        lenient().when(appProperties.getDomain()).thenReturn("test.com");

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
    void saveAnswer_정상() {
        // given
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
    void saveAnswer_파일첨부_정상() {
        // given
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
        FileInfo fileInfo = FileInfo.from(file, "test.com");
        CommunityAnswerFile communityAnswerFile = CommunityAnswerFile.create(communityAnswer, file);

        when(communityReader.getCommunityQuestionDetailsById(questionId)).thenReturn(communityQuestion);
        when(userReader.getUser(userId)).thenReturn(user);
        when(communityWriter.saveAnswer(any(CommunityQuestion.class), any(), any(User.class))).thenReturn(communityAnswer);
        when(communityFileManager.saveAnswerImageFiles(any(), any())).thenReturn(List.of(fileInfo));
        when(communityReader.getCommunityAnswerFilesByAnswerId(anyString())).thenReturn(List.of(communityAnswerFile));
        when(communityFileManager.convertAnswerFilesToFileInfos(List.of(communityAnswerFile))).thenReturn(List.of(fileInfo));
        lenient().when(appProperties.getDomain()).thenReturn("test.com");

        //when
        CommunityAnswerRes communityAnswerRes = communityService.saveAnswer(req, imageFiles, null);

        //then
        assertThat(communityAnswerRes).isNotNull();
        assertThat(communityAnswerRes.content()).isEqualTo(content);
        assertThat(communityAnswerRes.attachments()).isNotNull();
        assertThat(communityAnswerRes.attachments().size()).isEqualTo(1);
    }

    @Test
    void deleteAnswer_정상() {
        // given
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
    void updateAnswer_정상() {
        // given
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
    void updateAnswer_파일첨부_정상() {
        // given
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
        FileInfo fileInfo = FileInfo.from(file, "test.com");
        CommunityAnswerFile communityAnswerFile = CommunityAnswerFile.create(updatedCommunityAnswer, file);

        when(communityReader.getCommunityAnswerById(answerId)).thenReturn(communityAnswer);
        when(communityWriter.updateAnswer(any(CommunityAnswer.class), any())).thenReturn(updatedCommunityAnswer);
        when(communityFileManager.saveAnswerImageFiles(any(), any())).thenReturn(List.of(fileInfo));
        when(communityReader.getCommunityAnswerFilesByAnswerId(answerId)).thenReturn(List.of(communityAnswerFile));
        when(communityFileManager.convertAnswerFilesToFileInfos(List.of(communityAnswerFile))).thenReturn(List.of(fileInfo));
        lenient().when(appProperties.getDomain()).thenReturn("test.com");

        //when
        CommunityAnswerRes communityAnswerRes = communityService.updateAnswer(req, imageFiles, null);

        //then
        assertThat(communityAnswerRes).isNotNull();
        assertThat(communityAnswerRes.content()).isEqualTo(content);
        assertThat(communityAnswerRes.attachments()).isNotNull();
        assertThat(communityAnswerRes.attachments().size()).isEqualTo(1);
    }

    @Test
    void searchQuestions_정상() {
        // 검색 조건에 따른 질문 검색이 올바르게 동작하는지 검증

        // given
        CommunityQuestionSearchReq req = new CommunityQuestionSearchReq(1, 10, 1L, true, "파이썬", "createdAt:desc");

        CommunityQuestion question1 = CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser(1L, "파이썬 질문1", "내용1");
        CommunityQuestion question2 = CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser(2L, "파이썬 질문2", "내용2");
        List<CommunityQuestionRes> questionResList = List.of(
                CommunityQuestionRes.create(1L, 1L, "파이썬 질문1", "내용1", null, null, List.of(), List.of(), null),
                CommunityQuestionRes.create(1L, 1L, "파이썬 질문2", "내용2", null, null, List.of(), List.of(), null)
        );

        when(communityComplexReader.searchQuestions(any(), any(), any())).thenReturn(questionResList);
        when(communityComplexReader.countSearchQuestions(any(), any())).thenReturn(new PaginationRes(2, 1, 1, 10));

        // when
        SearchRes<CommunityQuestionRes> result = communityService.searchQuestions(req);

        // then
        assertThat(result).isNotNull();
        assertThat(result.items()).hasSize(2);
        assertThat(result.pagination().totalItems()).isEqualTo(2);
        assertThat(result.pagination().currentPage()).isEqualTo(1);
        assertThat(result.pagination().perPage()).isEqualTo(10);
        assertThat(result.items().get(0).title()).isEqualTo("파이썬 질문1");
        assertThat(result.items().get(1).title()).isEqualTo("파이썬 질문2");
    }

    @Test
    void searchQuestions_빈_결과() {
        // 검색 결과가 없을 때 빈 리스트가 반환되는지 검증

        // given
        CommunityQuestionSearchReq req = new CommunityQuestionSearchReq(1, 10, null, null, "존재하지 않는 키워드", "createdAt:desc");

        when(communityComplexReader.searchQuestions(any(), any(), any())).thenReturn(List.of());
        when(communityComplexReader.countSearchQuestions(any(), any())).thenReturn(new PaginationRes(0, 0, 1, 10));

        // when
        SearchRes<CommunityQuestionRes> result = communityService.searchQuestions(req);

        // then
        assertThat(result).isNotNull();
        assertThat(result.items()).isEmpty();
        assertThat(result.pagination().totalItems()).isEqualTo(0);
        assertThat(result.pagination().totalPages()).isEqualTo(0);
    }

    @Test
    void searchQuestions_페이지네이션_검증() {
        // 페이지네이션이 올바르게 적용되는지 검증

        // given
        CommunityQuestionSearchReq req = new CommunityQuestionSearchReq(2, 5, null, null, null, "createdAt:desc");

        CommunityQuestionRes questionRes = CommunityQuestionRes.create(1L, 1L, "질문", "내용", null, null, List.of(), List.of(), null);
        List<CommunityQuestionRes> questionResList = List.of(questionRes);

        when(communityComplexReader.searchQuestions(any(), any(), any())).thenReturn(questionResList);
        when(communityComplexReader.countSearchQuestions(any(), any())).thenReturn(new PaginationRes(10, 2, 2, 5));

        // when
        SearchRes<CommunityQuestionRes> result = communityService.searchQuestions(req);

        // then
        assertThat(result).isNotNull();
        assertThat(result.pagination().currentPage()).isEqualTo(2);
        assertThat(result.pagination().perPage()).isEqualTo(5);
        assertThat(result.pagination().totalItems()).isEqualTo(10);
        assertThat(result.pagination().totalPages()).isEqualTo(2);
    }
}



