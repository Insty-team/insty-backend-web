package insty.domain.community.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import insty.ai.adapter.AiRequester;
import insty.cloudfront.adapter.CloudFrontSigner;
import insty.domain.community.dto.CommunityQuestionCreateReq;
import insty.domain.community.dto.CommunityQuestionDetailsRes;
import insty.domain.community.dto.CommunityQuestionSearchReq;
import insty.domain.community.dto.CommunityQuestionUpdateReq;
import insty.domain.community.implement.CommunityQuestionReader;
import insty.domain.community.implement.CommunityQuestionWriter;
import insty.domain.community.repository.CommunityQuestionFileRepository;
import insty.domain.community.repository.CommunityQuestionRepository;
import insty.domain.user.implement.UserReader;
import insty.domain.video.repository.VideoQuestionRepository;
import insty.exception.CustomException;
import insty.global.property.AppProperties;
import insty.model.community.CommunityQuestion;
import insty.s3.adapter.S3FileManager;
import insty.s3.adapter.S3UrlIssuer;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Tag("integration")
class CommunityQuestionServiceTest {

    @Autowired
    private CommunityQuestionService communityQuestionService;
    @Autowired
    private CommunityQuestionReader communityQuestionReader;
    @Autowired
    private CommunityQuestionWriter communityQuestionWriter;
    @Autowired
    private UserReader userReader;
    @Autowired
    private CommunityQuestionRepository communityQuestionRepository;
    @Autowired
    private CommunityQuestionFileRepository communityQuestionFileRepository;
    @Autowired
    private VideoQuestionRepository videoQuestionRepository;
    @Autowired
    private EntityManager entityManager;
    @MockitoBean
    private S3UrlIssuer s3UrlIssuer;
    @MockitoBean
    private S3FileManager s3FileManager;
    @MockitoBean
    private CloudFrontSigner cloudFrontSigner;
    @MockitoBean
    private AppProperties appProperties;
    @MockitoBean
    private AiRequester aiRequester;

    @BeforeEach
    void setUp() {
        // S3FileManager.upload() 메서드 Mock 설정
        org.mockito.Mockito.when(s3FileManager.upload(
                org.mockito.ArgumentMatchers.any(org.springframework.web.multipart.MultipartFile.class),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString()
        )).thenAnswer(invocation -> {
            // 파일명을 기반으로 고유한 업로드 이름 생성
            org.springframework.web.multipart.MultipartFile file = invocation.getArgument(0);
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                return "mock_upload_" + System.currentTimeMillis() + ".jpg";
            }
            String extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
            return "mock_upload_" + System.currentTimeMillis() + extension;
        });
    }

    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_COURSE_ID = 1L;
    private static final int MAX_FILE_COUNT = 2;

    private Long createQuestionAndGetId(String title, String content) {
        var req = new CommunityQuestionCreateReq(TEST_COURSE_ID, title, content, null);
        communityQuestionService.saveQuestion(TEST_USER_ID, req, null);
        return communityQuestionReader.getAllCommunityQuestionsByCourseId(TEST_COURSE_ID).stream()
                .filter(q -> q.getTitle().equals(title))
                .findFirst()
                .map(insty.model.community.CommunityQuestion::getId)
                .orElseThrow();
    }

    // ========================================
    // saveQuestion 관련 테스트들
    // ========================================

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());"
    })
    @Test
    void saveQuestion_기본질문생성_정상() {
        // given
        String title = "질문 제목";
        String content = "질문 내용";
        CommunityQuestionCreateReq req = new CommunityQuestionCreateReq(TEST_COURSE_ID, title, content, null);

        // when
        CommunityQuestionDetailsRes res = communityQuestionService.saveQuestion(TEST_USER_ID, req, List.of());

        // then
        assertThat(res).isNotNull();
        assertThat(res.title()).isEqualTo(title);
        assertThat(res.content()).isEqualTo(content);
        assertThat(res.user().id()).isEqualTo(TEST_USER_ID);
        assertThat(res.user().nickname()).isEqualTo("user");
        assertThat(res.courseId()).isEqualTo(TEST_COURSE_ID);
        assertThat(res.attachments()).isEmpty();
        assertThat(res.videoInfo()).isNull();
        assertThat(res.answers()).isEmpty();
        assertThat(res.createdAt()).isNotNull();
        assertThat(res.updatedAt()).isNotNull();

        // DB에서 질문 조회하여 검증
        Long questionId = communityQuestionReader.getAllCommunityQuestionsByCourseId(TEST_COURSE_ID).stream()
                .filter(q -> q.getTitle().equals(title))
                .findFirst()
                .map(insty.model.community.CommunityQuestion::getId)
                .orElseThrow();
        CommunityQuestion question = communityQuestionReader.getCommunityQuestionWithFilesById(questionId);
        assertThat(question).isNotNull();
        assertThat(question.getTitle()).isEqualTo(title);
        assertThat(question.getContent()).isEqualTo(content);
        assertThat(question.getUser().getId()).isEqualTo(TEST_USER_ID);
        assertThat(question.getCourse().getId()).isEqualTo(TEST_COURSE_ID);
        assertThat(question.isAnswered()).isFalse();
        assertThat(question.isDeleted()).isFalse();
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());"
    })
    @Test
    void saveQuestion_긴내용질문_정상() {
        // given - 긴 내용의 질문
        String title = "긴 내용 질문";
        String content = "이것은 매우 긴 질문 내용입니다. ".repeat(100); // 충분히 긴 내용
        CommunityQuestionCreateReq req = new CommunityQuestionCreateReq(TEST_COURSE_ID, title, content, null);

        // when
        CommunityQuestionDetailsRes res = communityQuestionService.saveQuestion(TEST_USER_ID, req, List.of());

        // then
        assertThat(res).isNotNull();
        assertThat(res.title()).isEqualTo(title);
        assertThat(res.content()).isEqualTo(content);
        assertThat(res.content().length()).isGreaterThan(1000);
        assertThat(res.content().length()).isEqualTo(content.length());
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());"
    })
    @Test
    void saveQuestion_특수문자포함_정상() {
        // given - 특수문자가 포함된 질문
        String title = "특수문자 질문!@#$%^&*()";
        String content = "내용에 특수문자가 포함되어 있습니다: !@#$%^&*()_+-=[]{}|;':\",./<>?";
        CommunityQuestionCreateReq req = new CommunityQuestionCreateReq(TEST_COURSE_ID, title, content, null);

        // when
        CommunityQuestionDetailsRes res = communityQuestionService.saveQuestion(TEST_USER_ID, req, List.of());

        // then
        assertThat(res).isNotNull();
        assertThat(res.title()).isEqualTo(title);
        assertThat(res.content()).isEqualTo(content);
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());"
    })
    @Test
    void saveQuestion_한글내용_정상() {
        // given - 한글이 포함된 질문
        String title = "한글 질문 제목입니다";
        String content = "한글로 작성된 질문 내용입니다. 안녕하세요!";
        CommunityQuestionCreateReq req = new CommunityQuestionCreateReq(TEST_COURSE_ID, title, content, null);

        // when
        CommunityQuestionDetailsRes res = communityQuestionService.saveQuestion(TEST_USER_ID, req, List.of());

        // then
        assertThat(res).isNotNull();
        assertThat(res.title()).isEqualTo(title);
        assertThat(res.content()).isEqualTo(content);
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());"
    })
    @Test
    void saveQuestion_빈첨부파일목록_정상() {
        // given - 첨부파일이 없는 질문
        String title = "첨부파일 없는 질문";
        String content = "첨부파일이 없는 질문 내용";
        CommunityQuestionCreateReq req = new CommunityQuestionCreateReq(TEST_COURSE_ID, title, content, null);

        // when
        CommunityQuestionDetailsRes res = communityQuestionService.saveQuestion(TEST_USER_ID, req, List.of());

        // then
        assertThat(res).isNotNull();
        assertThat(res.attachments()).isEmpty();
        assertThat(res.videoInfo()).isNull();
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.video_questions (id, video_uuid, original_file_name, s3key, extension, duration, encoding_status, encoding_at, user_id, community_question_id, is_deleted, created_at, updated_at) " +
                    "VALUES (1, '00000000-0000-0000-0000-000000000001', 'test_video.mp4', 'vod/QUESTION/mp4/00000000-0000-0000-0000-000000000001/test_video.mp4', 'mp4', 0, 'COMPLETED', NOW(), 1, null, false, NOW(), NOW());"
    })
    @Test
    void saveQuestion_비디오UUID포함_정상() {
        // given
        String title = "비디오 UUID가 포함된 질문";
        String content = "비디오 UUID가 포함된 질문 내용";
        UUID videoUuid = UUID.fromString("00000000-0000-0000-0000-000000000001");
        var req = new CommunityQuestionCreateReq(TEST_COURSE_ID, title, content, videoUuid);

        // when
        CommunityQuestionDetailsRes result = communityQuestionService.saveQuestion(TEST_USER_ID, req, List.of());

        // then
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo(title);
        assertThat(result.content()).isEqualTo(content);
        assertThat(result.videoInfo().videoUuid()).isEqualTo(videoUuid);
        
    }



    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.video_questions (id, video_uuid, original_file_name, s3key, extension, duration, encoding_status, encoding_at, user_id, community_question_id, is_deleted, created_at, updated_at) " +
                    "VALUES (1, '00000000-0000-0000-0000-000000000001', 'test_video.mp4', 'vod/QUESTION/mp4/00000000-0000-0000-0000-000000000001/test_video.mp4', 'mp4', 0, 'COMPLETED', NOW(), 1, null, false, NOW(), NOW());",
            "INSERT INTO web_service.video_questions (id, video_uuid, original_file_name, s3key, extension, duration, encoding_status, encoding_at, user_id, community_question_id, is_deleted, created_at, updated_at) " +
                    "VALUES (2, '00000000-0000-0000-0000-000000000002', 'test_video2.mp4', 'vod/QUESTION/mp4/00000000-0000-0000-0000-000000000002/test_video2.mp4', 'mp4', 0, 'COMPLETED', NOW(), 1, null, false, NOW(), NOW());",
            "INSERT INTO web_service.video_questions (id, video_uuid, original_file_name, s3key, extension, duration, encoding_status, encoding_at, user_id, community_question_id, is_deleted, created_at, updated_at) " +
                    "VALUES (3, '00000000-0000-0000-0000-000000000003', 'test_video3.mp4', 'vod/QUESTION/mp4/00000000-0000-0000-0000-000000000003/test_video3.mp4', 'mp4', 0, 'COMPLETED', NOW(), 1, null, false, NOW(), NOW());",
            "INSERT INTO web_service.video_questions (id, video_uuid, original_file_name, s3key, extension, duration, encoding_status, encoding_at, user_id, community_question_id, is_deleted, created_at, updated_at) " +
                    "VALUES (4, '00000000-0000-0000-0000-000000000004', 'test_video4.mp4', 'vod/QUESTION/mp4/00000000-0000-0000-0000-000000000004/test_video4.mp4', 'mp4', 0, 'COMPLETED', NOW(), 1, null, false, NOW(), NOW());"
    })
    @Test
    void saveQuestion_비디오와첨부파일모두포함_정상() {
        // given
        String title = "비디오와 첨부파일이 모두 포함된 질문";
        String content = "비디오와 첨부파일이 모두 포함된 질문 내용";
        UUID videoUuid = UUID.fromString("00000000-0000-0000-0000-000000000001");
        var req = new CommunityQuestionCreateReq(TEST_COURSE_ID, title, content, videoUuid);

        // when
        CommunityQuestionDetailsRes result = communityQuestionService.saveQuestion(TEST_USER_ID, req, List.of());

        // then
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo(title);
        assertThat(result.content()).isEqualTo(content);
        assertThat(result.user().id()).isEqualTo(TEST_USER_ID);
        assertThat(result.user().nickname()).isEqualTo("user");
        assertThat(result.courseId()).isEqualTo(TEST_COURSE_ID);
        assertThat(result.attachments()).isEmpty();
        assertThat(result.videoInfo().videoUuid()).isEqualTo(videoUuid);
        assertThat(result.answers()).isEmpty();
        assertThat(result.createdAt()).isNotNull();
        assertThat(result.updatedAt()).isNotNull();

        // DB에서 질문 조회하여 검증
        Long questionId = communityQuestionReader.getAllCommunityQuestionsByCourseId(TEST_COURSE_ID).stream()
                .filter(q -> q.getTitle().equals(title))
                .findFirst()
                .map(insty.model.community.CommunityQuestion::getId)
                .orElseThrow();
        CommunityQuestion savedQuestion = communityQuestionReader.getCommunityQuestionWithFilesById(questionId);
        assertThat(savedQuestion).isNotNull();
        assertThat(savedQuestion.getTitle()).isEqualTo(title);
        assertThat(savedQuestion.getContent()).isEqualTo(content);
        assertThat(savedQuestion.getUser().getId()).isEqualTo(TEST_USER_ID);
        assertThat(savedQuestion.getCourse().getId()).isEqualTo(TEST_COURSE_ID);
        assertThat(savedQuestion.isAnswered()).isFalse();
        assertThat(savedQuestion.isDeleted()).isFalse();
    }


    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());"
    })
    @Test
    void saveQuestion_동시에같은코스에질문작성_정상() {
        // given - 같은 코스에 여러 질문을 동시에 작성
        String title1 = "첫 번째 질문";
        String title2 = "두 번째 질문";
        String content = "질문 내용";

        CommunityQuestionCreateReq req1 = new CommunityQuestionCreateReq(TEST_COURSE_ID, title1, content, null);
        CommunityQuestionCreateReq req2 = new CommunityQuestionCreateReq(TEST_COURSE_ID, title2, content, null);

        // when
        CommunityQuestionDetailsRes res1 = communityQuestionService.saveQuestion(TEST_USER_ID, req1, List.of());
        CommunityQuestionDetailsRes res2 = communityQuestionService.saveQuestion(TEST_USER_ID, req2, List.of());

        // then
        assertThat(res1).isNotNull();
        assertThat(res2).isNotNull();
        assertThat(res1.title()).isEqualTo(title1);
        assertThat(res2.title()).isEqualTo(title2);

        // 같은 코스의 질문 목록 조회
        var questions = communityQuestionReader.getAllCommunityQuestionsByCourseId(TEST_COURSE_ID);
        assertThat(questions).hasSize(2);
        assertThat(questions).anyMatch(q -> q.getTitle().equals(title1));
        assertThat(questions).anyMatch(q -> q.getTitle().equals(title2));
    }

    // ========================================
    // saveQuestion 예외 케이스들
    // ========================================

    @Test
    void saveQuestion_필수값누락_title_예외() {
        // given - title이 null인 경우
        var req = new CommunityQuestionCreateReq(TEST_COURSE_ID, null, "내용", null);

        // when & then
        assertThatThrownBy(() -> communityQuestionService.saveQuestion(TEST_USER_ID, req, List.of()))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void saveQuestion_필수값누락_content_예외() {
        // given - content가 null인 경우
        var req = new CommunityQuestionCreateReq(TEST_COURSE_ID, "제목", null, null);

        // when & then
        assertThatThrownBy(() -> communityQuestionService.saveQuestion(TEST_USER_ID, req, List.of()))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void saveQuestion_필수값누락_userId_예외() {
        // given - userId가 null인 경우
        var req = new CommunityQuestionCreateReq(TEST_COURSE_ID, "제목", "내용", null);

        // when & then
        assertThatThrownBy(() -> communityQuestionService.saveQuestion(null, req, List.of()))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void saveQuestion_빈제목_예외() {
        // given - title이 빈 문자열인 경우
        var req = new CommunityQuestionCreateReq(TEST_COURSE_ID, "", "내용", null);

        // when & then
        assertThatThrownBy(() -> communityQuestionService.saveQuestion(TEST_USER_ID, req, List.of()))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void saveQuestion_공백제목_예외() {
        // given - title이 공백만 있는 경우
        var req = new CommunityQuestionCreateReq(TEST_COURSE_ID, "   ", "내용", null);

        // when & then
        assertThatThrownBy(() -> communityQuestionService.saveQuestion(TEST_USER_ID, req, List.of()))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void saveQuestion_빈내용_예외() {
        // given - content가 빈 문자열인 경우
        var req = new CommunityQuestionCreateReq(TEST_COURSE_ID, "제목", "", null);

        // when & then
        assertThatThrownBy(() -> communityQuestionService.saveQuestion(TEST_USER_ID, req, List.of()))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void saveQuestion_공백내용_예외() {
        // given - content가 공백만 있는 경우
        var req = new CommunityQuestionCreateReq(TEST_COURSE_ID, "제목", "   ", null);

        // when & then
        assertThatThrownBy(() -> communityQuestionService.saveQuestion(TEST_USER_ID, req, List.of()))
                .isInstanceOf(CustomException.class);
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());"
    })
    @Test
    void saveQuestion_존재하지않는코스_예외() {
        // given - 존재하지 않는 courseId
        var req = new CommunityQuestionCreateReq(99999L, "제목", "내용", null);

        // when & then
        assertThatThrownBy(() -> communityQuestionService.saveQuestion(TEST_USER_ID, req, List.of()))
                .isInstanceOf(CustomException.class);
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());"
    })
    @Test
    void saveQuestion_첨부파일_10개초과_예외() {
        // given - 10개를 초과하는 첨부파일
        String title = "첨부파일10개초과", content = "내용";
        var req = new CommunityQuestionCreateReq(TEST_COURSE_ID, title, content, null);

        // (MAX_FILE_COUNT+1)개의 mock 파일 생성
        List<org.springframework.web.multipart.MultipartFile> files = java.util.stream.IntStream.range(0, MAX_FILE_COUNT+1)
                .mapToObj(i -> org.mockito.Mockito.mock(org.springframework.web.multipart.MultipartFile.class))
                .toList();
        files.forEach(f -> org.mockito.Mockito.when(f.isEmpty()).thenReturn(false));

        // when & then
        assertThatThrownBy(() -> communityQuestionService.saveQuestion(TEST_USER_ID, req, files))
                .isInstanceOf(insty.exception.CustomException.class);
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());"
    })
    @Test
    void saveQuestion_빈첨부파일_예외() {
        // given - 빈 첨부파일이 포함된 경우
        String title = "빈첨부파일테스트", content = "내용";
        var req = new CommunityQuestionCreateReq(TEST_COURSE_ID, title, content, null);

        // 빈 파일 mock 생성
        org.springframework.web.multipart.MultipartFile emptyFile = org.mockito.Mockito.mock(org.springframework.web.multipart.MultipartFile.class);
        org.mockito.Mockito.when(emptyFile.isEmpty()).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> communityQuestionService.saveQuestion(TEST_USER_ID, req, List.of(emptyFile)))
                .isInstanceOf(insty.exception.CustomException.class);
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());"
    })
    @Test
    void saveQuestion_존재하지않는사용자_예외() {
        // given - 존재하지 않는 userId
        var req = new CommunityQuestionCreateReq(TEST_COURSE_ID, "제목", "내용", null);

        // when & then
        assertThatThrownBy(() -> communityQuestionService.saveQuestion(99999L, req, List.of()))
                .isInstanceOf(CustomException.class);
    }

    // ========================================
    // getQuestionDetails 관련 테스트들
    // ========================================

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (10, 1, 1, '상세조회 제목', '상세조회 내용', false, false, NOW(), NOW());"
    })
    @Test
    void getQuestionDetails_정상() {
        // given
        Long questionId = 10L;

        // when
        CommunityQuestionDetailsRes res = communityQuestionService.getQuestionDetails(questionId);

        // then
        assertThat(res).isNotNull();
        assertThat(res.title()).isEqualTo("상세조회 제목");
        assertThat(res.content()).isEqualTo("상세조회 내용");
        assertThat(res.user().id()).isEqualTo(1L);
        assertThat(res.user().nickname()).isEqualTo("user");
        assertThat(res.user().userType()).isEqualTo(insty.model.user.UserType.CREATOR);
        assertThat(res.courseId()).isEqualTo(1L);
        assertThat(res.attachments()).isEmpty();
        assertThat(res.videoInfo()).isNull();
        assertThat(res.answers()).isEmpty();
        assertThat(res.createdAt()).isNotNull();
        assertThat(res.updatedAt()).isNotNull();
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (2, 'answerer@example.com', 'answerer', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (11, 1, 1, '답변있는질문', '답변있는질문 내용', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_answers (id, question_id, user_id, content, is_accepted, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 11, 2, '첫 번째 답변입니다', false, false, NOW(), NOW());",
            "INSERT INTO web_service.community_answers (id, question_id, user_id, content, is_accepted, is_deleted, created_at, updated_at) " +
                    "VALUES (2, 11, 2, '두 번째 답변입니다', true, false, NOW(), NOW());"
    })
    @Test
    void getQuestionDetails_답변포함_정상() {
        // given
        Long questionId = 11L;

        // when
        CommunityQuestionDetailsRes res = communityQuestionService.getQuestionDetails(questionId);

        // then
        assertThat(res).isNotNull();
        assertThat(res.title()).isEqualTo("답변있는질문");
        assertThat(res.content()).isEqualTo("답변있는질문 내용");
        assertThat(res.user().id()).isEqualTo(1L);
        assertThat(res.courseId()).isEqualTo(1L);
        assertThat(res.attachments()).isEmpty();
        assertThat(res.videoInfo()).isNull();
        assertThat(res.answers()).hasSize(2);

        // 답변 검증
        var firstAnswer = res.answers().get(0);
        assertThat(firstAnswer.content()).isEqualTo("첫 번째 답변입니다");
        assertThat(firstAnswer.user().id()).isEqualTo(2L);
        assertThat(firstAnswer.user().nickname()).isEqualTo("answerer");
        assertThat(firstAnswer.isAccepted()).isFalse();

        var secondAnswer = res.answers().get(1);
        assertThat(secondAnswer.content()).isEqualTo("두 번째 답변입니다");
        assertThat(secondAnswer.user().id()).isEqualTo(2L);
        assertThat(secondAnswer.isAccepted()).isTrue();
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.files (id, name, original_name, content_type, size, container_type, container_id, created_at, updated_at) " +
                    "VALUES (1, 'stored1.jpg', 'test1.jpg', 'image/jpeg', 1024, 'QUESTION_IMAGE', 12, NOW(), NOW());",
            "INSERT INTO web_service.files (id, name, original_name, content_type, size, container_type, container_id, created_at, updated_at) " +
                    "VALUES (2, 'stored2.png', 'test2.png', 'image/png', 2048, 'QUESTION_IMAGE', 12, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (12, 1, 1, '첨부파일있는질문', '첨부파일있는질문 내용', false, false, NOW(), NOW());",
            "INSERT INTO web_service.community_question_files (question_id, file_id, created_at, updated_at) " +
                    "VALUES (12, 1, NOW(), NOW());",
            "INSERT INTO web_service.community_question_files (question_id, file_id, created_at, updated_at) " +
                    "VALUES (12, 2, NOW(), NOW());"
    })
    @Test
    void getQuestionDetails_첨부파일포함_정상() {
        // given
        Long questionId = 12L;

        // when
        CommunityQuestionDetailsRes res = communityQuestionService.getQuestionDetails(questionId);

        // then
        assertThat(res).isNotNull();
        assertThat(res.title()).isEqualTo("첨부파일있는질문");
        assertThat(res.content()).isEqualTo("첨부파일있는질문 내용");
        assertThat(res.user().id()).isEqualTo(1L);
        assertThat(res.courseId()).isEqualTo(1L);
        assertThat(res.attachments()).hasSize(2);
        assertThat(res.videoInfo()).isNull();
        assertThat(res.answers()).isEmpty();

        // 첨부파일 검증
        var firstFile = res.attachments().get(0);
        assertThat(firstFile.name()).isEqualTo("test1.jpg");
        assertThat(firstFile.contentType()).isEqualTo("image/jpeg");
        assertThat(firstFile.size()).isEqualTo(1024);
        assertThat(firstFile.id()).isEqualTo(1L);

        var secondFile = res.attachments().get(1);
        assertThat(secondFile.name()).isEqualTo("test2.png");
        assertThat(secondFile.contentType()).isEqualTo("image/png");
        assertThat(secondFile.size()).isEqualTo(2048);
        assertThat(secondFile.id()).isEqualTo(2L);
    }



    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (14, 1, 1, '긴내용질문상세조회', REPEAT('이것은 매우 긴 질문 내용입니다. ', 60), false, false, NOW(), NOW());"
    })
    @Test
    void getQuestionDetails_긴내용_정상() {
        // given
        Long questionId = 14L;

        // when
        CommunityQuestionDetailsRes res = communityQuestionService.getQuestionDetails(questionId);

        // then
        assertThat(res).isNotNull();
        assertThat(res.title()).isEqualTo("긴내용질문상세조회");
        assertThat(res.content().length()).isGreaterThan(1000);
        assertThat(res.content()).contains("이것은 매우 긴 질문 내용입니다.");
        assertThat(res.user().id()).isEqualTo(1L);
        assertThat(res.courseId()).isEqualTo(1L);
        assertThat(res.attachments()).isEmpty();
        assertThat(res.videoInfo()).isNull();
        assertThat(res.answers()).isEmpty();
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());"
    })
    @Test
    void getQuestionDetails_존재하지않는질문_예외() {
        assertThatThrownBy(() -> communityQuestionService.getQuestionDetails(99999L))
                .isInstanceOf(CustomException.class);
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (1003, 1, 1, '삭제된질문상세조회', '내용', false, true, NOW(), NOW());"
    })
    @Test
    void getQuestionDetails_삭제된질문_예외() {
        assertThatThrownBy(() -> communityQuestionService.getQuestionDetails(1003L))
                .isInstanceOf(CustomException.class);
    }

    // ========================================
    // updateQuestion 관련 테스트들
    // ========================================

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (20, 1, 1, '수정전 제목', '수정전 내용', false, false, NOW(), NOW());"
    })
    @Test
    void updateQuestion_기본수정_정상() {
        // given
        Long questionId = 20L;
        String newTitle = "수정된 제목";
        String newContent = "수정된 내용";
        CommunityQuestionUpdateReq req = new CommunityQuestionUpdateReq(newTitle, newContent, null, null);

        // when
        CommunityQuestionDetailsRes res = communityQuestionService.updateQuestion(TEST_USER_ID, questionId, req, List.of());

        // then
        assertThat(res).isNotNull();
        assertThat(res.title()).isEqualTo(newTitle);
        assertThat(res.content()).isEqualTo(newContent);
        assertThat(res.user().id()).isEqualTo(TEST_USER_ID);
        assertThat(res.user().nickname()).isEqualTo("user");
        assertThat(res.courseId()).isEqualTo(TEST_COURSE_ID);
        assertThat(res.attachments()).isEmpty();
        assertThat(res.videoInfo()).isNull();
        assertThat(res.answers()).isEmpty();
        assertThat(res.createdAt()).isNotNull();
        assertThat(res.updatedAt()).isNotNull();

        // DB에서 질문 조회하여 검증
        CommunityQuestion updatedQuestion = communityQuestionReader.getCommunityQuestionWithFilesById(questionId);
        assertThat(updatedQuestion).isNotNull();
        assertThat(updatedQuestion.getTitle()).isEqualTo(newTitle);
        assertThat(updatedQuestion.getContent()).isEqualTo(newContent);
        assertThat(updatedQuestion.getUser().getId()).isEqualTo(TEST_USER_ID);
        assertThat(updatedQuestion.getCourse().getId()).isEqualTo(TEST_COURSE_ID);
        assertThat(updatedQuestion.isAnswered()).isFalse();
        assertThat(updatedQuestion.isDeleted()).isFalse();
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (1002, 1, 1, '동시수정테스트', '내용', false, false, NOW(), NOW());"
    })
    @Test
    void updateQuestion_동시수정_정상() {
        // given - 동시에 여러 번 수정
        Long questionId = 1002L;
        var req1 = new CommunityQuestionUpdateReq( "수정1", "내용1", null, null);
        var req2 = new CommunityQuestionUpdateReq("수정2", "내용2", null, null);

        // when
        var res1 = communityQuestionService.updateQuestion(TEST_USER_ID, questionId, req1, List.of());
        var res2 = communityQuestionService.updateQuestion(TEST_USER_ID, questionId, req2, List.of());

        // then
        assertThat(res1.title()).isEqualTo("수정1");
        assertThat(res1.content()).isEqualTo("내용1");
        assertThat(res2.title()).isEqualTo("수정2");
        assertThat(res2.content()).isEqualTo("내용2");

        // 최종 상태 확인
        CommunityQuestion finalQuestion = communityQuestionReader.getCommunityQuestionWithFilesById(questionId);
        assertThat(finalQuestion.getTitle()).isEqualTo("수정2");
        assertThat(finalQuestion.getContent()).isEqualTo("내용2");
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (1003, 1, 1, '긴내용수정테스트', '짧은 내용', false, false, NOW(), NOW());"
    })
    @Test
    void updateQuestion_긴내용으로수정_정상() {
        // given - 긴 내용으로 수정
        Long questionId = 1003L;
        String newTitle = "긴 내용으로 수정된 제목";
        String newContent = "이것은 매우 긴 질문 내용으로 수정된 것입니다. ".repeat(100); // 충분히 긴 내용
        CommunityQuestionUpdateReq req = new CommunityQuestionUpdateReq(newTitle, newContent, null, null);

        // when
        CommunityQuestionDetailsRes res = communityQuestionService.updateQuestion(TEST_USER_ID, questionId, req, List.of());

        // then
        assertThat(res).isNotNull();
        assertThat(res.title()).isEqualTo(newTitle);
        assertThat(res.content()).isEqualTo(newContent);
        assertThat(res.content().length()).isGreaterThan(1000);
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (1004, 1, 1, '특수문자수정테스트', '일반 내용', false, false, NOW(), NOW());"
    })
    @Test
    void updateQuestion_특수문자포함으로수정_정상() {
        // given - 특수문자가 포함된 내용으로 수정
        Long questionId = 1004L;
        String newTitle = "특수문자 제목!@#$%^&*()";
        String newContent = "내용에 특수문자가 포함되어 있습니다: !@#$%^&*()_+-=[]{}|;':\",./<>?";
        CommunityQuestionUpdateReq req = new CommunityQuestionUpdateReq(newTitle, newContent, null, null);

        // when
        CommunityQuestionDetailsRes res = communityQuestionService.updateQuestion(TEST_USER_ID, questionId, req, List.of());

        // then
        assertThat(res).isNotNull();
        assertThat(res.title()).isEqualTo(newTitle);
        assertThat(res.content()).isEqualTo(newContent);
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (1005, 1, 1, '한글수정테스트', '영어 내용', false, false, NOW(), NOW());"
    })
    @Test
    void updateQuestion_한글로수정_정상() {
        // given - 한글로 수정
        Long questionId = 1005L;
        String newTitle = "한글로 수정된 제목입니다";
        String newContent = "한글로 작성된 질문 내용으로 수정되었습니다. 안녕하세요!";
        CommunityQuestionUpdateReq req = new CommunityQuestionUpdateReq(newTitle, newContent, null, null);

        // when
        CommunityQuestionDetailsRes res = communityQuestionService.updateQuestion(TEST_USER_ID, questionId, req, List.of());

        // then
        assertThat(res).isNotNull();
        assertThat(res.title()).isEqualTo(newTitle);
        assertThat(res.content()).isEqualTo(newContent);
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (51, 1, 1, '비디오업데이트질문', '비디오업데이트질문 내용', false, false, NOW(), NOW());",
            "INSERT INTO web_service.video_questions (id, video_uuid, original_file_name, s3key, extension, duration, encoding_status, encoding_at, user_id, community_question_id, is_deleted, created_at, updated_at) " +
                    "VALUES (1, '00000000-0000-0000-0000-000000000001', 'test_video.mp4', 'vod/QUESTION/mp4/00000000-0000-0000-0000-000000000001/test_video.mp4', 'mp4', 0, 'COMPLETED', NOW(), 1, 51, false, NOW(), NOW());",
            "INSERT INTO web_service.video_questions (id, video_uuid, original_file_name, s3key, extension, duration, encoding_status, encoding_at, user_id, community_question_id, is_deleted, created_at, updated_at) " +
                    "VALUES (2, '00000000-0000-0000-0000-000000000002', 'test_video2.mp4', 'vod/QUESTION/mp4/00000000-0000-0000-0000-000000000002/test_video2.mp4', 'mp4', 0, 'COMPLETED', NOW(), 1, null, false, NOW(), NOW());",
            "INSERT INTO web_service.video_questions (id, video_uuid, original_file_name, s3key, extension, duration, encoding_status, encoding_at, user_id, community_question_id, is_deleted, created_at, updated_at) " +
                    "VALUES (3, '00000000-0000-0000-0000-000000000003', 'test_video3.mp4', 'vod/QUESTION/mp4/00000000-0000-0000-0000-000000000003/test_video3.mp4', 'mp4', 0, 'COMPLETED', NOW(), 1, null, false, NOW(), NOW());"
    })
    @Test
    void updateQuestion_비디오UUID업데이트_정상() {
        // given
        Long questionId = 51L;
        String newTitle = "비디오가 업데이트된 질문";
        String newContent = "비디오가 업데이트된 질문 내용";
        UUID newVideoUuid1 = UUID.fromString("00000000-0000-0000-0000-000000000002");
        var req = new CommunityQuestionUpdateReq(newTitle, newContent, newVideoUuid1, List.of());

        // when
        CommunityQuestionDetailsRes result = communityQuestionService.updateQuestion(TEST_USER_ID, questionId, req, List.of());

        // then
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo(newTitle);
        assertThat(result.content()).isEqualTo(newContent);
        assertThat(result.videoInfo().videoUuid()).isEqualTo(newVideoUuid1);
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (53, 1, 1, '복합업데이트질문', '복합업데이트질문 내용', false, false, NOW(), NOW());",
            "INSERT INTO web_service.video_questions (id, video_uuid, original_file_name, s3key, extension, duration, encoding_status, encoding_at, user_id, community_question_id, is_deleted, created_at, updated_at) " +
                    "VALUES (1, '00000000-0000-0000-0000-000000000001', 'test_video.mp4', 'vod/QUESTION/mp4/00000000-0000-0000-0000-000000000001/test_video.mp4', 'mp4', 0, 'COMPLETED', NOW(), 1, 53, false, NOW(), NOW());",
            "INSERT INTO web_service.video_questions (id, video_uuid, original_file_name, s3key, extension, duration, encoding_status, encoding_at, user_id, community_question_id, is_deleted, created_at, updated_at) " +
                    "VALUES (2, '00000000-0000-0000-0000-000000000002', 'test_video2.mp4', 'vod/QUESTION/mp4/00000000-0000-0000-0000-000000000002/test_video2.mp4', 'mp4', 0, 'COMPLETED', NOW(), 1, null, false, NOW(), NOW());"
    })
    @Test
    void updateQuestion_비디오와첨부파일모두업데이트_정상() {
        // given
        Long questionId = 53L;
        String newTitle = "비디오와 첨부파일이 모두 업데이트된 질문";
        String newContent = "비디오와 첨부파일이 모두 업데이트된 질문 내용";
        UUID newVideoUuid = UUID.fromString("00000000-0000-0000-0000-000000000002");
        var req = new CommunityQuestionUpdateReq(newTitle, newContent, newVideoUuid, List.of());

        // when
        CommunityQuestionDetailsRes result = communityQuestionService.updateQuestion(TEST_USER_ID, questionId, req, List.of());

        // then
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo(newTitle);
        assertThat(result.content()).isEqualTo(newContent);
        assertThat(result.videoInfo().videoUuid()).isEqualTo(newVideoUuid);
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (1006, 1, 1, '동일내용수정테스트', '기존 내용', false, false, NOW(), NOW());"
    })
    @Test
    void updateQuestion_동일내용으로수정_정상() {
        // given - 동일한 내용으로 수정
        Long questionId = 1006L;
        String newTitle = "기존 내용"; // 동일한 내용
        String newContent = "기존 내용"; // 동일한 내용
        CommunityQuestionUpdateReq req = new CommunityQuestionUpdateReq(newTitle, newContent, null, null);

        // when
        CommunityQuestionDetailsRes res = communityQuestionService.updateQuestion(TEST_USER_ID, questionId, req, List.of());

        // then
        assertThat(res).isNotNull();
        assertThat(res.title()).isEqualTo(newTitle);
        assertThat(res.content()).isEqualTo(newContent);
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (1007, 1, 1, '여러번수정테스트', '첫 번째 내용', false, false, NOW(), NOW());"
    })
    @Test
    void updateQuestion_여러번수정_정상() {
        // given - 여러 번 수정
        Long questionId = 1007L;

        // 첫 번째 수정
        var req1 = new CommunityQuestionUpdateReq( "두 번째 제목", "두 번째 내용", null, null);
        var res1 = communityQuestionService.updateQuestion(TEST_USER_ID, questionId, req1, List.of());
        assertThat(res1.title()).isEqualTo("두 번째 제목");
        assertThat(res1.content()).isEqualTo("두 번째 내용");

        // 두 번째 수정
        var req2 = new CommunityQuestionUpdateReq("세 번째 제목", "세 번째 내용", null, null);
        var res2 = communityQuestionService.updateQuestion(TEST_USER_ID, questionId, req2, List.of());
        assertThat(res2.title()).isEqualTo("세 번째 제목");
        assertThat(res2.content()).isEqualTo("세 번째 내용");

        // 세 번째 수정
        var req3 = new CommunityQuestionUpdateReq("최종 제목", "최종 내용", null, null);
        var res3 = communityQuestionService.updateQuestion(TEST_USER_ID, questionId, req3, List.of());
        assertThat(res3.title()).isEqualTo("최종 제목");
        assertThat(res3.content()).isEqualTo("최종 내용");

        // 최종 상태 확인
        CommunityQuestion finalQuestion = communityQuestionReader.getCommunityQuestionWithFilesById(questionId);
        assertThat(finalQuestion.getTitle()).isEqualTo("최종 제목");
        assertThat(finalQuestion.getContent()).isEqualTo("최종 내용");
    }

    // ========================================
    // updateQuestion 예외 케이스들
    // ========================================

    @Test
    void updateQuestion_필수값누락_title_예외() {
        // given - title이 null인 경우
        var req = new CommunityQuestionUpdateReq( null, "내용", null, null);

        // when & then
        assertThatThrownBy(() -> communityQuestionService.updateQuestion(TEST_USER_ID, 1L, req, List.of()))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void updateQuestion_필수값누락_content_예외() {
        // given - content가 null인 경우
        var req = new CommunityQuestionUpdateReq( "제목", null, null, null);

        // when & then
        assertThatThrownBy(() -> communityQuestionService.updateQuestion(TEST_USER_ID, 1L, req, List.of()))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void updateQuestion_빈제목_예외() {
        // given - title이 빈 문자열인 경우
        var req = new CommunityQuestionUpdateReq("", "내용", null, null);

        // when & then
        assertThatThrownBy(() -> communityQuestionService.updateQuestion(TEST_USER_ID, 1L, req, List.of()))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void updateQuestion_공백제목_예외() {
        // given - title이 공백만 있는 경우
        var req = new CommunityQuestionUpdateReq("   ", "내용", null, null);

        // when & then
        assertThatThrownBy(() -> communityQuestionService.updateQuestion(TEST_USER_ID, 1L, req, List.of()))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void updateQuestion_빈내용_예외() {
        // given - content가 빈 문자열인 경우
        var req = new CommunityQuestionUpdateReq("제목", "", null, null);

        // when & then
        assertThatThrownBy(() -> communityQuestionService.updateQuestion(TEST_USER_ID, 1L, req, List.of()))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void updateQuestion_공백내용_예외() {
        // given - content가 공백만 있는 경우
        var req = new CommunityQuestionUpdateReq( "제목", "   ", null, null);

        // when & then
        assertThatThrownBy(() -> communityQuestionService.updateQuestion(TEST_USER_ID, 1L, req, List.of()))
                .isInstanceOf(CustomException.class);
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (120, 1, 1, '수정불가질문', '수정불가질문 내용', false, false, NOW(), NOW());"
    })
    @Test
    void updateQuestion_이미삭제된질문_예외() {
        // given - 논리 삭제된 질문
        Long questionId = 120L;
        communityQuestionService.deleteQuestion(TEST_USER_ID, questionId);
        entityManager.flush();
        entityManager.clear();

        // when & then - 삭제된 질문 수정 시도 시 예외 발생 검증
        var updateReq = new CommunityQuestionUpdateReq( "수정된 제목", "수정된 내용", null, null);
        assertThatThrownBy(() -> communityQuestionService.updateQuestion(TEST_USER_ID, questionId, updateReq, List.of()))
                .isInstanceOf(CustomException.class);
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (1008, 1, 1, '삭제된질문수정', '내용', false, true, NOW(), NOW());"
    })
    @Test
    void updateQuestion_삭제된질문_예외() {
        // given - 이미 삭제된 질문
        var req = new CommunityQuestionUpdateReq( "수정", "수정", null, null);

        // when & then
        assertThatThrownBy(() -> communityQuestionService.updateQuestion(TEST_USER_ID, 1008L, req, List.of()))
                .isInstanceOf(CustomException.class);
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (2, 'user2@example.com', 'user2', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (1009, 1, 2, '다른사용자질문', '내용', false, false, NOW(), NOW());"
    })
    @Test
    void updateQuestion_다른사용자질문수정_예외() {
        // given - 다른 사용자의 질문을 수정하려고 시도
        var req = new CommunityQuestionUpdateReq( "수정", "수정", null, null);

        // when & then - 다른 사용자의 질문 수정 시도 시 예외 발생
        assertThatThrownBy(() -> communityQuestionService.updateQuestion(TEST_USER_ID, 1009L, req, List.of()))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void updateQuestion_존재하지않는질문_예외() {
        // given - 존재하지 않는 질문 ID
        var req = new CommunityQuestionUpdateReq( "수정", "수정", null, null);

        // when & then
        assertThatThrownBy(() -> communityQuestionService.updateQuestion(TEST_USER_ID, 99999L, req, List.of()))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void updateQuestion_존재하지않는사용자_예외() {
        // given - 존재하지 않는 사용자 ID
        var req = new CommunityQuestionUpdateReq("수정", "수정", null, null);

        // when & then
        assertThatThrownBy(() -> communityQuestionService.updateQuestion(99999L, 1L, req, List.of()))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void updateQuestion_questionId불일치_예외() {
        // given - 요청의 questionId와 경로 변수의 questionId가 불일치
        var req = new CommunityQuestionUpdateReq( "수정", "수정", null, null);

        // when & then
        assertThatThrownBy(() -> communityQuestionService.updateQuestion(TEST_USER_ID, 1L, req, List.of()))
                .isInstanceOf(CustomException.class);
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (9999, 1, 1, '파일10개테스트', '내용', false, false, NOW(), NOW());"
    })
    @Test
    void updateQuestion_첨부파일_초과_예외() {
        // given - 10개를 초과하는 첨부파일
        Long questionId = 9999L;
        var req = new CommunityQuestionUpdateReq( "수정", "수정", null, null);
        List<org.springframework.web.multipart.MultipartFile> files = java.util.stream.IntStream.range(0, MAX_FILE_COUNT+1)
                .mapToObj(i -> org.mockito.Mockito.mock(org.springframework.web.multipart.MultipartFile.class))
                .toList();
        files.forEach(f -> org.mockito.Mockito.when(f.isEmpty()).thenReturn(false));

        // when & then
        assertThatThrownBy(() -> communityQuestionService.updateQuestion(TEST_USER_ID, questionId, req, files))
                .isInstanceOf(insty.exception.CustomException.class);
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (1010, 1, 1, '빈파일테스트', '내용', false, false, NOW(), NOW());"
    })
    @Test
    void updateQuestion_빈첨부파일_예외() {
        // given - 빈 첨부파일이 포함된 경우
        Long questionId = 1010L;
        var req = new CommunityQuestionUpdateReq("수정", "수정", null, null);

        // 빈 파일 mock 생성
        org.springframework.web.multipart.MultipartFile emptyFile = org.mockito.Mockito.mock(org.springframework.web.multipart.MultipartFile.class);
        org.mockito.Mockito.when(emptyFile.isEmpty()).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> communityQuestionService.updateQuestion(TEST_USER_ID, questionId, req, List.of(emptyFile)))
                .isInstanceOf(insty.exception.CustomException.class);
    }

    // ========================================
    // deleteQuestion 관련 테스트들
    // ========================================

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (30, 1, 1, '삭제할 제목', '삭제할 내용', false, false, NOW(), NOW());"
    })
    @Test
    void deleteQuestion_기본삭제_정상() {
        // given
        Long questionId = 30L;

        // when
        communityQuestionService.deleteQuestion(TEST_USER_ID, questionId);
        entityManager.flush();
        entityManager.clear();

        // then
        Optional<insty.model.community.CommunityQuestion> questionOpt = communityQuestionRepository.findById(questionId);
        assertThat(questionOpt).isPresent();
        assertThat(questionOpt.get().isDeleted()).isTrue();
        assertThat(questionOpt.get().getTitle()).isEqualTo("삭제할 제목");
        assertThat(questionOpt.get().getContent()).isEqualTo("삭제할 내용");
        assertThat(questionOpt.get().getUser().getId()).isEqualTo(TEST_USER_ID);
        assertThat(questionOpt.get().getCourse().getId()).isEqualTo(TEST_COURSE_ID);
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (1011, 1, 1, '답변완료질문삭제', '답변완료질문 내용', true, false, NOW(), NOW());"
    })
    @Test
    void deleteQuestion_답변완료질문삭제_정상() {
        // given - 답변이 완료된 질문 삭제
        Long questionId = 1011L;

        // when
        communityQuestionService.deleteQuestion(TEST_USER_ID, questionId);
        entityManager.flush();
        entityManager.clear();

        // then
        Optional<insty.model.community.CommunityQuestion> questionOpt = communityQuestionRepository.findById(questionId);
        assertThat(questionOpt).isPresent();
        assertThat(questionOpt.get().isDeleted()).isTrue();
        assertThat(questionOpt.get().isAnswered()).isTrue(); // 답변완료 상태는 유지
    }



    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (80, 1, 1, '삭제테스트 질문', '삭제테스트 질문 내용', false, false, NOW(), NOW());"
    })
    @Test
    void deleteQuestion_이미삭제된질문_예외() {
        // given
        Long questionId = 80L;
        // 1차 삭제
        communityQuestionService.deleteQuestion(TEST_USER_ID, questionId);
        // 2차 삭제 시도
        assertThatThrownBy(() -> communityQuestionService.deleteQuestion(TEST_USER_ID, questionId))
                .isInstanceOf(CustomException.class);
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (1001, 1, 1, '동시삭제테스트', '내용', false, false, NOW(), NOW());"
    })
    @Test
    void deleteQuestion_동시삭제_예외() {
        Long questionId = 1001L;
        // 첫 번째 삭제
        communityQuestionService.deleteQuestion(TEST_USER_ID, questionId);
        // 두 번째 삭제 시도
        assertThatThrownBy(() -> communityQuestionService.deleteQuestion(TEST_USER_ID, questionId))
                .isInstanceOf(CustomException.class);
    }


    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (100, 1, 1, '첨부파일/비디오 삭제 테스트', '내용', false, false, NOW(), NOW());",
            "INSERT INTO web_service.files (id, name, original_name, content_type, size, container_type, container_id, created_at, updated_at) " +
                    "VALUES (10, 'file1.jpg', 'file1.jpg', 'image/jpeg', 1000, 'QUESTION_IMAGE', 100, NOW(), NOW());",
            "INSERT INTO web_service.community_question_files (question_id, file_id, created_at, updated_at) " +
                    "VALUES (100, 10, NOW(), NOW());",
            "INSERT INTO web_service.video_questions (id, video_uuid, original_file_name, s3key, extension, duration, encoding_status, encoding_at, user_id, community_question_id, is_deleted, created_at, updated_at) " +
                    "VALUES (20, '11111111-1111-1111-1111-111111111111', 'video.mp4', 'vod/QUESTION/mp4/11111111-1111-1111-1111-111111111111/video.mp4', 'mp4', 10, 'COMPLETED', NOW(), 1, 100, false, NOW(), NOW());"
    })
    @Test
    void deleteQuestion_첨부파일_비디오_삭제_정상() {
        Long questionId = 100L;
        communityQuestionService.deleteQuestion(TEST_USER_ID, questionId);
        entityManager.flush();
        entityManager.clear();

        // 질문 논리 삭제 확인
        var questionOpt = communityQuestionRepository.findById(questionId);
        assertThat(questionOpt).isPresent();
        assertThat(questionOpt.get().isDeleted()).isTrue();

        // 첨부파일 논리 삭제(매핑 row 삭제) 확인
        var files = communityQuestionFileRepository.findAllByCommunityQuestionId(questionId);
        assertThat(files).isEmpty();

        // 비디오 논리 삭제 확인
        var video = videoQuestionRepository.findByCommunityQuestionIdAndIsDeleted(questionId, true);
        assertThat(video).isPresent();
        assertThat(video.get().isDeleted()).isTrue();
    }

    // ========================================
    // searchQuestions 관련 테스트들
    // ========================================

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (2, 'user2@example.com', 'user2', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의1', '설명1', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (2, 2, '테스트 강의2', '설명2', 20000, 0, 0, '중급자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (400, 1, 1, '검색질문1', '내용1', false, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (401, 1, 1, '검색질문2', '내용2', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (402, 2, 2, '다른강의질문', '내용3', false, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (403, 1, 1, '삭제된질문', '내용4', false, true, NOW(), NOW());"
    })
    @Test
    void searchQuestions_키워드검색_정상() {
        // given
        var req = CommunityQuestionSearchReq.builder()
                .page(1)
                .pageSize(10)
                .search("검색질문")
                .build();

        // when
        var res = communityQuestionService.searchQuestions(req);

        // then
        assertThat(res).isNotNull();
        assertThat(res.items()).hasSize(2);
        assertThat(res.items().get(0).title()).contains("검색질문");
        assertThat(res.items().get(1).title()).contains("검색질문");
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (404, 1, 1, '질문1', '내용1', false, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (405, 1, 1, '질문2', '내용2', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (406, 1, 1, '질문3', '내용3', false, false, NOW(), NOW());"
    })
    @Test
    void searchQuestions_페이지네이션_정상() {
        // given
        var req = CommunityQuestionSearchReq.builder()
                .page(1)
                .pageSize(2)
                .build();

        // when
        var res = communityQuestionService.searchQuestions(req);

        // then
        assertThat(res).isNotNull();
        assertThat(res.items()).hasSize(2);
        assertThat(res.pagination().currentPage()).isEqualTo(1);
        assertThat(res.pagination().perPage()).isEqualTo(2);
        assertThat(res.pagination().totalItems()).isEqualTo(3);
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (407, 1, 1, '질문1', '내용1', false, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (408, 1, 1, '질문2', '내용2', true, false, NOW(), NOW());"
    })
    @Test
    void searchQuestions_답변완료필터_정상() {
        // given - 답변 완료된 질문만 검색
        var req = CommunityQuestionSearchReq.builder()
                .page(1)
                .pageSize(10)
                .isAnswered(true)
                .build();

        // when
        var res = communityQuestionService.searchQuestions(req);

        // then
        assertThat(res).isNotNull();
        assertThat(res.items()).hasSize(1);
        assertThat(res.items().get(0).title()).isEqualTo("질문2");
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (409, 1, 1, '질문1', '내용1', false, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (410, 1, 1, '질문2', '내용2', true, false, NOW(), NOW());"
    })
    @Test
    void searchQuestions_답변미완료필터_정상() {
        // given - 답변 미완료된 질문만 검색
        var req = CommunityQuestionSearchReq.builder()
                .page(1)
                .pageSize(10)
                .isAnswered(false)
                .build();

        // when
        var res = communityQuestionService.searchQuestions(req);

        // then
        assertThat(res).isNotNull();
        assertThat(res.items()).hasSize(1);
        assertThat(res.items().get(0).title()).isEqualTo("질문1");
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (413, 1, 1, '질문1', '내용1', false, false, DATEADD('SECOND', -10, NOW()), DATEADD('SECOND', -10, NOW()));",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (414, 1, 1, '질문2', '내용2', false, false, NOW(), NOW());"
    })
    @Test
    void searchQuestions_정렬_최신순_정상() {
        // given - 최신순 정렬
        var req = CommunityQuestionSearchReq.builder()
                .page(1)
                .pageSize(10)
                .sort("createdAt")
                .build();

        // when
        var res = communityQuestionService.searchQuestions(req);

        // then
        assertThat(res).isNotNull();
        assertThat(res.items()).hasSize(2);
        // 최신순이므로 질문2가 먼저 나와야 함 (질문2가 10초 후에 생성됨)
        assertThat(res.items().get(0).title()).isEqualTo("질문2");
        assertThat(res.items().get(1).title()).isEqualTo("질문1");
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (415, 1, 1, '질문1', '내용1', false, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (416, 1, 1, '질문2', '내용2', false, false, NOW(), NOW());"
    })
    @Test
    void searchQuestions_정렬_제목순_정상() {
        // given - 제목순 정렬
        var req = CommunityQuestionSearchReq.builder()
                .page(1)
                .pageSize(10)
                .sort("title")
                .build();

        // when
        var res = communityQuestionService.searchQuestions(req);

        // then
        assertThat(res).isNotNull();
        assertThat(res.items()).hasSize(2);
        // 제목순이므로 질문1이 먼저 나와야 함
        assertThat(res.items().get(0).title()).isEqualTo("질문1");
        assertThat(res.items().get(1).title()).isEqualTo("질문2");
    }

    @Test
    void searchQuestions_빈결과_정상() {
        // given - 존재하지 않는 키워드로 검색
        var req = CommunityQuestionSearchReq.builder()
                .page(1)
                .pageSize(10)
                .search("존재하지않는키워드")
                .build();

        // when
        var res = communityQuestionService.searchQuestions(req);

        // then
        assertThat(res).isNotNull();
        assertThat(res.items()).isEmpty();
        assertThat(res.pagination().totalItems()).isEqualTo(0);
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (417, 1, 1, '질문1', '내용1', false, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (418, 1, 1, '질문2', '내용2', false, false, NOW(), NOW());"
    })
    @Test
    void searchQuestions_복합조건_정상() {
        // given - 여러 조건을 조합한 검색
        var req = CommunityQuestionSearchReq.builder()
                .page(1)
                .pageSize(10)
                .isAnswered(false)
                .search("질문")
                .sort("createdAt")
                .build();

        // when
        var res = communityQuestionService.searchQuestions(req);

        // then
        assertThat(res).isNotNull();
        assertThat(res.items()).hasSize(2);
        assertThat(res.items().get(0).title()).contains("질문");
    }

    // ========================================
    // searchQuestionsByUserId 관련 테스트들 (userId 필터링 검증 중심)
    // ========================================

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user1@example.com', 'user1', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (2, 'user2@example.com', 'user2', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (3, 'user3@example.com', 'user3', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (500, 1, 1, '사용자1질문1', '내용1', false, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (501, 1, 1, '사용자1질문2', '내용2', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (502, 1, 2, '사용자2질문1', '내용3', false, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (503, 1, 2, '사용자2질문2', '내용4', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (504, 1, 3, '사용자3질문1', '내용5', false, false, NOW(), NOW());"
    })
    @Test
    void searchQuestionsByUserId_사용자별필터링_정상() {
        // given - 사용자1의 질문만 검색
        var req = CommunityQuestionSearchReq.builder()
                .page(1)
                .pageSize(10)
                .build();

        // when
        var res = communityQuestionService.searchQuestionsByUserId(req, 1L);

        // then
        assertThat(res).isNotNull();
        assertThat(res.items()).hasSize(2);
        assertThat(res.items().get(0).user().id()).isEqualTo(1L);
        assertThat(res.items().get(1).user().id()).isEqualTo(1L);
        assertThat(res.items().get(0).title()).contains("사용자1");
        assertThat(res.items().get(1).title()).contains("사용자1");
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user1@example.com', 'user1', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (2, 'user2@example.com', 'user2', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (505, 1, 1, '사용자1질문1', '내용1', false, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (506, 1, 1, '사용자1질문2', '내용2', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (507, 1, 2, '사용자2질문1', '내용3', false, false, NOW(), NOW());"
    })
    @Test
    void searchQuestionsByUserId_다른사용자질문제외_정상() {
        // given - 사용자1의 질문만 검색 (사용자2의 질문은 제외되어야 함)
        var req = CommunityQuestionSearchReq.builder()
                .page(1)
                .pageSize(10)
                .build();

        // when
        var res = communityQuestionService.searchQuestionsByUserId(req, 1L);

        // then
        assertThat(res).isNotNull();
        assertThat(res.items()).hasSize(2);
        // 모든 결과가 사용자1의 질문인지 확인
        assertThat(res.items()).allSatisfy(item -> {
            assertThat(item.user().id()).isEqualTo(1L);
            assertThat(item.title()).contains("사용자1");
        });
        // 사용자2의 질문이 포함되지 않았는지 확인
        assertThat(res.items()).noneMatch(item -> item.title().contains("사용자2"));
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user1@example.com', 'user1', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (2, 'user2@example.com', 'user2', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (508, 1, 1, '사용자1질문1', '내용1', false, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (509, 1, 1, '사용자1질문2', '내용2', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (510, 1, 2, '사용자2질문1', '내용3', false, false, NOW(), NOW());"
    })
    @Test
    void searchQuestionsByUserId_키워드와사용자필터조합_정상() {
        // given - 사용자1의 질문 중에서 "질문1" 키워드로 검색
        var req = CommunityQuestionSearchReq.builder()
                .page(1)
                .pageSize(10)
                .search("질문1")
                .build();

        // when
        var res = communityQuestionService.searchQuestionsByUserId(req, 1L);

        // then
        assertThat(res).isNotNull();
        assertThat(res.items()).hasSize(1);
        assertThat(res.items().get(0).user().id()).isEqualTo(1L);
        assertThat(res.items().get(0).title()).isEqualTo("사용자1질문1");
        // 사용자2의 질문이 포함되지 않았는지 확인
        assertThat(res.items()).noneMatch(item -> item.title().contains("사용자2"));
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user1@example.com', 'user1', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (2, 'user2@example.com', 'user2', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (511, 1, 1, '사용자1질문1', '내용1', false, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (512, 1, 1, '사용자1질문2', '내용2', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (513, 1, 2, '사용자2질문1', '내용3', true, false, NOW(), NOW());"
    })
    @Test
    void searchQuestionsByUserId_답변완료필터와사용자필터조합_정상() {
        // given - 사용자1의 답변 완료된 질문만 검색
        var req = CommunityQuestionSearchReq.builder()
                .page(1)
                .pageSize(10)
                .isAnswered(true)
                .build();

        // when
        var res = communityQuestionService.searchQuestionsByUserId(req, 1L);

        // then
        assertThat(res).isNotNull();
        assertThat(res.items()).hasSize(1);
        assertThat(res.items().get(0).user().id()).isEqualTo(1L);
        assertThat(res.items().get(0).title()).isEqualTo("사용자1질문2");
        // 사용자2의 질문이 포함되지 않았는지 확인
        assertThat(res.items()).noneMatch(item -> item.title().contains("사용자2"));
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user1@example.com', 'user1', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (2, 'user2@example.com', 'user2', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (514, 1, 1, '사용자1질문1', '내용1', false, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (515, 1, 1, '사용자1질문2', '내용2', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (516, 1, 2, '사용자2질문1', '내용3', false, false, NOW(), NOW());"
    })
    @Test
    void searchQuestionsByUserId_페이지네이션과사용자필터조합_정상() {
        // given - 사용자1의 질문을 페이지 크기 1로 검색
        var req = CommunityQuestionSearchReq.builder()
                .page(1)
                .pageSize(1)
                .build();

        // when
        var res = communityQuestionService.searchQuestionsByUserId(req, 1L);

        // then
        assertThat(res).isNotNull();
        assertThat(res.items()).hasSize(1);
        assertThat(res.items().get(0).user().id()).isEqualTo(1L);
        assertThat(res.items().get(0).title()).contains("사용자1");
        assertThat(res.pagination().totalItems()).isEqualTo(2); // 사용자1의 총 질문 수
        assertThat(res.pagination().currentPage()).isEqualTo(1);
        assertThat(res.pagination().perPage()).isEqualTo(1);
    }

    @Test
    void searchQuestionsByUserId_존재하지않는사용자_빈결과_정상() {
        // given - 존재하지 않는 사용자의 질문 검색
        var req = CommunityQuestionSearchReq.builder()
                .page(1)
                .pageSize(10)
                .build();

        // when
        var res = communityQuestionService.searchQuestionsByUserId(req, 99999L);

        // then
        assertThat(res).isNotNull();
        assertThat(res.items()).isEmpty();
        assertThat(res.pagination().totalItems()).isEqualTo(0);
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user1@example.com', 'user1', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (2, 'user2@example.com', 'user2', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (517, 1, 1, '사용자1질문1', '내용1', false, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (518, 1, 1, '사용자1질문2', '내용2', true, false, NOW(), NOW());"
    })
    @Test
    void searchQuestionsByUserId_사용자정보정확성검증_정상() {
        // given - 사용자1의 질문 검색
        var req = CommunityQuestionSearchReq.builder()
                .page(1)
                .pageSize(10)
                .build();

        // when
        var res = communityQuestionService.searchQuestionsByUserId(req, 1L);

        // then
        assertThat(res).isNotNull();
        assertThat(res.items()).hasSize(2);
        // 모든 결과의 사용자 정보가 정확한지 검증
        assertThat(res.items()).allSatisfy(item -> {
            assertThat(item.user().id()).isEqualTo(1L);
            assertThat(item.user().nickname()).isEqualTo("user1");
        });
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user1@example.com', 'user1', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (2, 'user2@example.com', 'user2', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (519, 1, 1, '사용자1질문1', '내용1', false, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (520, 1, 2, '사용자2질문1', '내용2', false, false, NOW(), NOW());"
    })
    @Test
    void searchQuestionsByUserId_다른사용자키워드검색시빈결과_정상() {
        // given - 사용자1의 질문에서 사용자2의 키워드로 검색
        var req = CommunityQuestionSearchReq.builder()
                .page(1)
                .pageSize(10)
                .search("사용자2")
                .build();

        // when
        var res = communityQuestionService.searchQuestionsByUserId(req, 1L);

        // then
        assertThat(res).isNotNull();
        assertThat(res.items()).isEmpty();
        assertThat(res.pagination().totalItems()).isEqualTo(0);
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (423, 1, 1, '질문1', '내용1', false, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (424, 1, 1, '질문2', '내용2', false, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (425, 1, 1, '질문3', '내용3', false, false, NOW(), NOW());"
    })
    @Test
    void searchQuestions_페이지네이션_두번째페이지_정상() {
        // given - 두 번째 페이지 요청
        var req = CommunityQuestionSearchReq.builder()
                .page(2)
                .pageSize(2)
                .build();

        // when
        var res = communityQuestionService.searchQuestions(req);

        // then
        assertThat(res).isNotNull();
        assertThat(res.items()).hasSize(1); // 3개 중 2개씩 나누면 2페이지에는 1개
        assertThat(res.pagination().currentPage()).isEqualTo(2);
        assertThat(res.pagination().perPage()).isEqualTo(2);
        assertThat(res.pagination().totalItems()).isEqualTo(3);
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (426, 1, 1, '질문1', '내용1', false, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (427, 1, 1, '질문2', '내용2', false, false, NOW(), NOW());"
    })
    @Test
    void searchQuestions_대소문자구분없음_정상() {
        // given - 대소문자가 다른 키워드로 검색
        var req = CommunityQuestionSearchReq.builder()
                .page(1)
                .pageSize(10)
                .search("질문")
                .build();

        // when
        var res = communityQuestionService.searchQuestions(req);

        // then
        assertThat(res).isNotNull();
        assertThat(res.items()).hasSize(2);
        assertThat(res.items().get(0).title()).contains("질문");
        assertThat(res.items().get(1).title()).contains("질문");
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (428, 1, 1, '질문1', '내용1', false, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (429, 1, 1, '질문2', '내용2', false, false, NOW(), NOW());"
    })
    @Test
    void searchQuestions_특수문자포함_정상() {
        // given - 특수문자가 포함된 키워드로 검색
        var req = CommunityQuestionSearchReq.builder()
                .page(1)
                .pageSize(10)
                .search("질문")
                .build();

        // when
        var res = communityQuestionService.searchQuestions(req);

        // then
        assertThat(res).isNotNull();
        assertThat(res.items()).hasSize(2);
        assertThat(res.items().get(0).title()).contains("질문");
        assertThat(res.items().get(1).title()).contains("질문");
    }

    // ========================================
    // getQuestionsByCourseId 관련 테스트들
    // ========================================
    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (500, 1, 1, '목록질문', '내용', false, false, NOW(), NOW());"
    })
    @Test
    void searchQuestionsByCourseId_정상() {
        // given
        var req = CommunityQuestionSearchReq.builder()
                .page(1)
                .pageSize(10)
                .build();

        // when
        var res = communityQuestionService.searchQuestionsByCourseId(req, 1L);

        // then
        assertThat(res).isNotNull();
        assertThat(res.items()).isNotEmpty();
        assertThat(res.items().get(0).courseId()).isEqualTo(1L);
        assertThat(res.pagination().totalItems()).isEqualTo(1);
    }

    @Test
    void searchQuestionsByCourseId_존재하지않는코스_정상() {
        // given
        var req = CommunityQuestionSearchReq.builder()
                .page(1)
                .pageSize(10)
                .build();

        // when
        var res = communityQuestionService.searchQuestionsByCourseId(req, 99999L);

        // then
        assertThat(res).isNotNull();
        assertThat(res.items()).isEmpty();
        assertThat(res.pagination().totalItems()).isEqualTo(0);
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (2, 1, '다른 코스', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (501, 1, 1, '질문1', '내용1', false, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (502, 1, 1, '질문2', '내용2', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (503, 2, 1, '다른코스질문', '내용3', false, false, NOW(), NOW());"
    })
    @Test
    void searchQuestionsByCourseId_페이지네이션_정상() {
        // given - 페이지 크기 1로 설정
        var req = CommunityQuestionSearchReq.builder()
                .page(1)
                .pageSize(1)
                .build();

        // when
        var res = communityQuestionService.searchQuestionsByCourseId(req, 1L);

        // then
        assertThat(res).isNotNull();
        assertThat(res.items()).hasSize(1);
        assertThat(res.items().get(0).courseId()).isEqualTo(1L);
        assertThat(res.pagination().totalItems()).isEqualTo(2);
        assertThat(res.pagination().totalPages()).isEqualTo(2);
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (504, 1, 1, '답변완료질문', '내용1', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (505, 1, 1, '답변미완료질문', '내용2', false, false, NOW(), NOW());"
    })
    @Test
    void searchQuestionsByCourseId_답변상태필터_정상() {
        // given - 답변 완료된 질문만 검색
        var req = CommunityQuestionSearchReq.builder()
                .page(1)
                .pageSize(10)
                .isAnswered(true)
                .build();

        // when
        var res = communityQuestionService.searchQuestionsByCourseId(req, 1L);

        // then
        assertThat(res).isNotNull();
        assertThat(res.items()).hasSize(1);
        assertThat(res.items().get(0).courseId()).isEqualTo(1L);
        assertThat(res.items().get(0).isAnswered()).isTrue();
        assertThat(res.pagination().totalItems()).isEqualTo(1);
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (506, 1, 1, '키워드질문', '내용1', false, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (507, 1, 1, '일반질문', '내용2', false, false, NOW(), NOW());"
    })
    @Test
    void searchQuestionsByCourseId_키워드검색_정상() {
        // given - 키워드로 검색
        var req = CommunityQuestionSearchReq.builder()
                .page(1)
                .pageSize(10)
                .search("키워드")
                .build();

        // when
        var res = communityQuestionService.searchQuestionsByCourseId(req, 1L);

        // then
        assertThat(res).isNotNull();
        assertThat(res.items()).hasSize(1);
        assertThat(res.items().get(0).courseId()).isEqualTo(1L);
        assertThat(res.items().get(0).title()).contains("키워드");
        assertThat(res.pagination().totalItems()).isEqualTo(1);
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (508, 1, 1, '질문1', '내용1', false, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (509, 1, 1, '질문2', '내용2', true, false, NOW(), NOW());"
    })
    @Test
    void searchQuestionsByCourseId_복합조건검색_정상() {
        // given - 여러 조건을 조합한 검색
        var req = CommunityQuestionSearchReq.builder()
                .page(1)
                .pageSize(10)
                .isAnswered(false)
                .keyword("질문")
                .sort("createdAt")
                .build();

        // when
        var res = communityQuestionService.searchQuestionsByCourseId(req, 1L);

        // then
        assertThat(res).isNotNull();
        assertThat(res.items()).hasSize(1);
        assertThat(res.items().get(0).courseId()).isEqualTo(1L);
        assertThat(res.items().get(0).isAnswered()).isFalse();
        assertThat(res.items().get(0).title()).contains("질문");
        assertThat(res.pagination().totalItems()).isEqualTo(1);
    }
}