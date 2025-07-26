package insty.domain.community.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import insty.ai.adapter.AiRequester;
import insty.domain.community.dto.CommunityAnswerCreateReq;
import insty.domain.community.dto.CommunityAnswerUpdateReq;
import insty.domain.community.implement.CommunityAnswerReader;
import insty.domain.community.implement.CommunityAnswerWriter;
import insty.domain.community.implement.CommunityQuestionReader;
import insty.domain.user.implement.UserReader;
import insty.model.community.CommunityAnswer;
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
import insty.exception.CustomException;
import insty.s3.adapter.S3FileManager;
import insty.s3.adapter.S3UrlIssuer;
import insty.cloudfront.adapter.CloudFrontSigner;
import insty.global.property.AppProperties;
import insty.domain.community.repository.CommunityAnswerRepository;
import insty.domain.community.repository.CommunityQuestionRepository;
import jakarta.persistence.EntityManager;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Tag("integration")
class CommunityAnswerServiceTest {

    @Autowired
    private CommunityAnswerService communityAnswerService;
    @Autowired
    private CommunityAnswerReader communityAnswerReader;
    @Autowired
    private CommunityAnswerWriter communityAnswerWriter;
    @Autowired
    private CommunityQuestionService communityQuestionService;
    @Autowired
    private CommunityQuestionReader communityQuestionReader;
    @Autowired
    private UserReader userReader;
    @Autowired
    private CommunityAnswerRepository communityAnswerRepository;
    @Autowired
    private CommunityQuestionRepository communityQuestionRepository;
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
    private static final int MAX_FILE_COUNT = 10;

    private Long createQuestionAndGetId(String title, String content) {
        var req = new insty.domain.community.dto.CommunityQuestionCreateReq(TEST_COURSE_ID, title, content, List.of());
        communityQuestionService.saveQuestion(TEST_USER_ID, req, List.of());
        return communityQuestionReader.getAllCommunityQuestionsByCourseId(TEST_COURSE_ID).stream()
                .filter(q -> q.getTitle().equals(title))
                .findFirst()
                .map(insty.model.community.CommunityQuestion::getId)
                .orElseThrow();
    }
    private Long createAnswerAndGetId(Long questionId, String content) {
        communityAnswerService.saveAnswer(TEST_USER_ID, new CommunityAnswerCreateReq(questionId, content, null), List.of());
        return communityAnswerReader.getAllCommunityAnswersByQuestionId(questionId).stream()
                .filter(a -> a.getContent().equals(content))
                .findFirst()
                .map(insty.model.community.CommunityAnswer::getId)
                .orElseThrow();
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (40, 1, 1, '답변용 질문', '답변용 질문 내용', false, false, NOW(), NOW());"
    })
    @Test
    void saveAnswer_정상() {
        // given
        Long questionId = 40L;
        String content = "답변 내용";
        var req = new CommunityAnswerCreateReq(questionId, content, null);

        // when
        var result = communityAnswerService.saveAnswer(TEST_USER_ID, req, List.of());

        // then
        assertThat(result).isNotNull();

        // User 정보 전체 검증
        assertThat(result.user()).isNotNull();
        assertThat(result.user().id()).isEqualTo(TEST_USER_ID);
        assertThat(result.user().nickname()).isEqualTo("user");
        assertThat(result.user().userType()).isEqualTo(insty.model.user.UserType.CREATOR);

        // 답변 내용 검증
        assertThat(result.content()).isEqualTo(content);
        assertThat(result.isAccepted()).isFalse();
        assertThat(result.attachments()).isEmpty();
        assertThat(result.videoInfo()).isNull();

        // 시간 정보 검증
        assertThat(result.createdAt()).isNotNull();
        assertThat(result.updatedAt()).isNotNull();
        assertThat(result.createdAt()).isEqualTo(result.updatedAt()); // 생성 시에는 같아야 함

        // DB에서 실제 저장 확인
        var savedAnswer = communityAnswerReader.getAllCommunityAnswersByQuestionId(questionId).stream()
                .filter(a -> a.getContent().equals(content))
                .findFirst()
                .orElse(null);

        assertThat(savedAnswer).isNotNull();
        assertThat(savedAnswer.getUser().getId()).isEqualTo(TEST_USER_ID);
        assertThat(savedAnswer.getCommunityQuestion().getId()).isEqualTo(questionId);
        assertThat(savedAnswer.getContent()).isEqualTo(content);
        assertThat(savedAnswer.isDeleted()).isFalse();
        assertThat(savedAnswer.isAccepted()).isFalse();
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (50, 1, 1, '답변수정 질문', '답변수정 질문 내용', false, false, NOW(), NOW());"
    })
    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (50, 1, 1, '답변수정 질문', '답변수정 질문 내용', false, false, NOW(), NOW());"
    })
    @Test
    void updateAnswer_정상() {
        // given
        Long questionId = 50L;
        String content = "원본 답변";
        var req = new CommunityAnswerCreateReq(questionId, content, null);
        var originalResult = communityAnswerService.saveAnswer(TEST_USER_ID, req, List.of());
        Long answerId = communityAnswerReader.getAllCommunityAnswersByQuestionId(questionId).stream()
                .filter(a -> a.getContent().equals(content))
                .findFirst()
                .map(insty.model.community.CommunityAnswer::getId)
                .orElseThrow();

        String newContent = "수정된 답변";
        var updateReq = new CommunityAnswerUpdateReq(newContent, null, List.of());

        // when
        var updatedRes = communityAnswerService.updateAnswer(TEST_USER_ID, answerId, updateReq, List.of());

        // then
        assertThat(updatedRes).isNotNull();

        // User 정보 검증
        assertThat(updatedRes.user()).isNotNull();
        assertThat(updatedRes.user().id()).isEqualTo(TEST_USER_ID);
        assertThat(updatedRes.user().nickname()).isEqualTo("user");
        assertThat(updatedRes.user().userType()).isEqualTo(insty.model.user.UserType.CREATOR);

        // 답변 내용 검증
        assertThat(updatedRes.content()).isEqualTo(newContent);
        assertThat(updatedRes.isAccepted()).isFalse();
        assertThat(updatedRes.attachments()).isEmpty();
        assertThat(updatedRes.videoInfo()).isNull();

        // 시간 정보 검증
        assertThat(updatedRes.createdAt()).isNotNull();
        assertThat(updatedRes.updatedAt()).isNotNull();
        assertThat(updatedRes.updatedAt()).isAfterOrEqualTo(updatedRes.createdAt());

        // DB에서 실제 수정 확인
        var savedAnswer = communityAnswerReader.getAllCommunityAnswersByQuestionId(questionId).stream()
                .filter(a -> a.getId().equals(answerId))
                .findFirst()
                .orElse(null);

        assertThat(savedAnswer).isNotNull();
        assertThat(savedAnswer.getContent()).isEqualTo(newContent);
        assertThat(savedAnswer.getUser().getId()).isEqualTo(TEST_USER_ID);
        assertThat(savedAnswer.isDeleted()).isFalse();
        assertThat(savedAnswer.isAccepted()).isFalse();
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (60, 1, 1, '답변삭제 질문', '답변삭제 질문 내용', false, false, NOW(), NOW());"
    })
    @Test
    void deleteAnswer_정상() {
        Long questionId = createQuestionAndGetId("답변삭제 질문", "답변삭제 질문 내용");
        String content = "삭제할 답변";
        Long answerId = createAnswerAndGetId(questionId, content);

        communityAnswerService.deleteAnswer(TEST_USER_ID, answerId);
        entityManager.flush();
        entityManager.clear();

        // then: 논리 삭제 후 DB에서 직접 조회하여 isDeleted=true 검증
        Optional<CommunityAnswer> answerOpt = communityAnswerRepository.findById(answerId);
        assertThat(answerOpt).isPresent();
        assertThat(answerOpt.get().isDeleted()).isTrue();
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (70, 1, 1, '채택질문', '채택질문 내용', false, false, NOW(), NOW());"
    })
    @Test
    void acceptAnswer_정상() {
        Long questionId = createQuestionAndGetId("채택질문", "채택질문 내용");
        String content = "채택할 답변";
        Long answerId = createAnswerAndGetId(questionId, content);

        var result = communityAnswerService.acceptAnswer(TEST_USER_ID, questionId, answerId);

        assertThat(result).isNotNull();
        assertThat(result.answerId()).isEqualTo(answerId);
        assertThat(result.accepted()).isTrue();
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (90, 1, 1, '답변삭제테스트 질문', '답변삭제테스트 질문 내용', false, false, NOW(), NOW());"
    })
    @Test
    void updateAnswer_이미삭제된답변_예외() {
        Long questionId = createQuestionAndGetId("답변삭제테스트 질문", "답변삭제테스트 질문 내용");
        String content = "삭제할 답변";
        Long answerId = createAnswerAndGetId(questionId, content);
        // 답변 논리 삭제
        communityAnswerService.deleteAnswer(TEST_USER_ID, answerId);
        entityManager.flush();
        entityManager.clear();
        // 삭제된 답변 수정 시도 시 예외 발생 검증
        var updateReq = new CommunityAnswerUpdateReq("수정", null, List.of());
        assertThatThrownBy(() -> communityAnswerService.updateAnswer(TEST_USER_ID, answerId, updateReq, List.of()))
                .isInstanceOf(CustomException.class);
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (100, 1, 1, '답변필수값테스트', '내용', false, false, NOW(), NOW());"
    })
    @Test
    void saveAnswer_content누락_예외() {
        Long questionId = createQuestionAndGetId("답변필수값테스트", "내용");
        var req = new CommunityAnswerCreateReq(questionId, null, null);
        assertThatThrownBy(() -> communityAnswerService.saveAnswer(TEST_USER_ID, req, List.of()))
                .isInstanceOf(CustomException.class);
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (102, 1, 1, '파일없는답변질문', '내용', false, false, NOW(), NOW());"
    })
    @Test
    void saveAnswer_첨부파일비디오없음_정상() {
        Long questionId = createQuestionAndGetId("파일없는답변질문", "내용");
        String content = "파일없는답변";
        var req = new CommunityAnswerCreateReq(questionId, content, null);
        var res = communityAnswerService.saveAnswer(TEST_USER_ID, req, List.of());
        assertThat(res).isNotNull();
        assertThat(res.attachments()).isEmpty();
        assertThat(res.videoInfo()).isNull();
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (600, 1, 1, '답변목록질문', '내용', false, false, NOW(), NOW());",
            "INSERT INTO web_service.files (id, container_type, container_id, name, original_name, content_type, size, created_at, updated_at) " +
                    "VALUES (100, 'ANSWER_IMAGE', 1, 'test_file1.jpg', 'original_test_file1.jpg', 'image/jpeg', 1024, NOW(), NOW());",
            "INSERT INTO web_service.files (id, container_type, container_id, name, original_name, content_type, size, created_at, updated_at) " +
                    "VALUES (101, 'ANSWER_IMAGE', 2, 'test_file2.png', 'original_test_file2.png', 'image/png', 2048, NOW(), NOW());"
    })
    @Test
    void getAllAnswers_ByQuestionId_정상() {
        Long questionId = 600L, userId = 1L;

        // 답변 2개 생성 (첨부파일 없는 답변과 첨부파일 있는 답변)
        communityAnswerService.saveAnswer(userId, new CommunityAnswerCreateReq(questionId, "답변1", null), List.of());
        communityAnswerService.saveAnswer(userId, new CommunityAnswerCreateReq(questionId, "답변2", null), List.of());

        var res = communityAnswerService.getAllAnswersByQuestionId(questionId);

        // 기본 검증
        assertThat(res).isNotNull();
        assertThat(res).hasSize(2);

        // 각 답변의 내용 검증
        var answer1 = res.stream().filter(a -> a.content().equals("답변1")).findFirst().orElse(null);
        var answer2 = res.stream().filter(a -> a.content().equals("답변2")).findFirst().orElse(null);

        assertThat(answer1).isNotNull();
        assertThat(answer2).isNotNull();

        // User 정보 검증
        assertThat(answer1.user()).isNotNull();
        assertThat(answer1.user().id()).isEqualTo(userId);
        assertThat(answer1.user().nickname()).isEqualTo("user");

        assertThat(answer2.user()).isNotNull();
        assertThat(answer2.user().id()).isEqualTo(userId);
        assertThat(answer2.user().nickname()).isEqualTo("user");

        // 첨부파일 정보 검증 (현재는 첨부파일이 없는 상태)
        assertThat(answer1.attachments()).isEmpty();
        assertThat(answer2.attachments()).isEmpty();
        assertThat(answer1.videoInfo()).isNull();
        assertThat(answer2.videoInfo()).isNull();
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (650, 1, 1, '첨부파일답변질문', '내용', false, false, NOW(), NOW());",
            "INSERT INTO web_service.community_answers (id, user_id, question_id, content, answer_image_id, created_at, updated_at, is_deleted, is_accepted) " +
                    "VALUES (1, 1, 650, '첨부파일있는답변', null, NOW(), NOW(), false, false);",
            "INSERT INTO web_service.files (id, container_type, container_id, name, original_name, content_type, size, created_at, updated_at) " +
                    "VALUES (200, 'ANSWER_IMAGE', 1, 'answer_file1.jpg', 'original_answer_file1.jpg', 'image/jpeg', 1024, NOW(), NOW());",
            "INSERT INTO web_service.community_answers_attachments (id, answer_id, file_id) " +
                    "VALUES (1, 1, 200);"
    })
    @Test
    void getAllAnswers_ByQuestionId_첨부파일포함_정상() {
        Long questionId = 650L;

        var res = communityAnswerService.getAllAnswersByQuestionId(questionId);

        // 기본 검증
        assertThat(res).isNotNull();
        assertThat(res).hasSize(1);

        var answer = res.get(0);
        assertThat(answer.content()).isEqualTo("첨부파일있는답변");

        // User 정보 검증
        assertThat(answer.user()).isNotNull();
        assertThat(answer.user().id()).isEqualTo(TEST_USER_ID);
        assertThat(answer.user().nickname()).isEqualTo("user");

        // 첨부파일 정보 검증
        assertThat(answer.attachments()).isNotEmpty();
        assertThat(answer.attachments()).hasSize(1);

        var attachment = answer.attachments().get(0);
        assertThat(attachment.name()).isEqualTo("original_answer_file1.jpg");
        assertThat(attachment.contentType()).isEqualTo("image/jpeg");
        assertThat(attachment.size()).isEqualTo(1024);
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (700, 1, 1, '존재하지않는질문테스트', '내용', false, false, NOW(), NOW());"
    })
    @Test
    void saveAnswer_존재하지않는질문_예외() {
        // 존재하지 않는 질문에 답변 작성 시 예외 발생
        var req = new CommunityAnswerCreateReq(99999L, "답변 내용", null);
        assertThatThrownBy(() -> communityAnswerService.saveAnswer(TEST_USER_ID, req, List.of()))
                .isInstanceOf(CustomException.class);
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (800, 1, 1, '삭제된질문테스트', '내용', false, true, NOW(), NOW());"
    })
    @Test
    void saveAnswer_삭제된질문_예외() {
        // 삭제된 질문에 답변 작성 시 예외 발생
        var req = new CommunityAnswerCreateReq(800L, "답변 내용", null);
        assertThatThrownBy(() -> communityAnswerService.saveAnswer(TEST_USER_ID, req, List.of()))
                .isInstanceOf(CustomException.class);
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (900, 1, 1, 'userId누락테스트', '내용', false, false, NOW(), NOW());"
    })
    @Test
    void saveAnswer_userId누락_예외() {
        // userId가 null인 경우 예외 발생

        // todo : 추후 처리할 것
        /*
        var req = new CommunityAnswerCreateReq(900L, "답변 내용", null);
        assertThatThrownBy(() -> communityAnswerService.saveAnswer(null, req, List.of()))
                .isInstanceOf(CustomException.class);

         */
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (1000, 1, 1, '빈내용테스트', '내용', false, false, NOW(), NOW());"
    })
    @Test
    void saveAnswer_빈내용_예외() {
        // content가 빈 문자열인 경우 예외 발생
        var req = new CommunityAnswerCreateReq(1000L, "", null);
        assertThatThrownBy(() -> communityAnswerService.saveAnswer(TEST_USER_ID, req, List.of()))
                .isInstanceOf(CustomException.class);
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (1100, 1, 1, 'updateAnswer테스트', '내용', false, false, NOW(), NOW());"
    })
    @Test
    void updateAnswer_존재하지않는답변_예외() {
        // 존재하지 않는 답변 수정 시 예외 발생
        var updateReq = new CommunityAnswerUpdateReq("수정된 내용", null, List.of());
        assertThatThrownBy(() -> communityAnswerService.updateAnswer(TEST_USER_ID, 99999L, updateReq, List.of()))
                .isInstanceOf(CustomException.class);
    }



    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (1300, 1, 1, 'deleteAnswer테스트', '내용', false, false, NOW(), NOW());"
    })
    @Test
    void deleteAnswer_존재하지않는답변_예외() {
        // 존재하지 않는 답변 삭제 시 예외 발생
        assertThatThrownBy(() -> communityAnswerService.deleteAnswer(TEST_USER_ID, 99999L))
                .isInstanceOf(CustomException.class);
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (1400, 1, 1, '중복삭제테스트', '내용', false, false, NOW(), NOW());"
    })
    @Test
    void deleteAnswer_이미삭제된답변_예외() {
        Long questionId = createQuestionAndGetId("중복삭제테스트", "내용");
        Long answerId = createAnswerAndGetId(questionId, "삭제할 답변");

        // 첫 번째 삭제는 성공
        communityAnswerService.deleteAnswer(TEST_USER_ID, answerId);
        entityManager.flush();
        entityManager.clear();

        // 두 번째 삭제 시 예외 발생
        assertThatThrownBy(() -> communityAnswerService.deleteAnswer(TEST_USER_ID, answerId))
                .isInstanceOf(CustomException.class);
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (1500, 1, 1, 'acceptAnswer테스트', '내용', false, false, NOW(), NOW());"
    })
    @Test
    void acceptAnswer_존재하지않는질문_예외() {
        Long questionId = createQuestionAndGetId("acceptAnswer테스트", "내용");
        Long answerId = createAnswerAndGetId(questionId, "채택할 답변");

        // 존재하지 않는 질문에 대해 답변 채택 시 예외 발생
        assertThatThrownBy(() -> communityAnswerService.acceptAnswer(TEST_USER_ID, 99999L, answerId))
                .isInstanceOf(CustomException.class);
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (1600, 1, 1, 'acceptAnswer존재하지않는답변테스트', '내용', false, false, NOW(), NOW());"
    })
    @Test
    void acceptAnswer_존재하지않는답변_예외() {
        Long questionId = createQuestionAndGetId("acceptAnswer존재하지않는답변테스트", "내용");

        // 존재하지 않는 답변 채택 시 예외 발생
        assertThatThrownBy(() -> communityAnswerService.acceptAnswer(TEST_USER_ID, questionId, 99999L))
                .isInstanceOf(CustomException.class);
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (1700, 1, 1, '다른질문답변채택테스트', '내용', false, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (1701, 1, 1, '다른질문', '내용', false, false, NOW(), NOW());"
    })
    @Test
    void acceptAnswer_다른질문의답변_예외() {
        Long questionId1 = createQuestionAndGetId("다른질문답변채택테스트", "내용");
        Long questionId2 = createQuestionAndGetId("다른질문", "내용");
        Long answerId = createAnswerAndGetId(questionId1, "답변");

        // 다른 질문의 답변을 채택하려고 시도 시 예외 발생
        assertThatThrownBy(() -> communityAnswerService.acceptAnswer(TEST_USER_ID, questionId2, answerId))
                .isInstanceOf(CustomException.class);
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (1800, 1, 1, '중복채택테스트', '내용', false, false, NOW(), NOW());"
    })
    @Test
    void acceptAnswer_이미채택된답변존재시다른답변채택_예외() {
        Long questionId = createQuestionAndGetId("중복채택테스트", "내용");
        Long answerId1 = createAnswerAndGetId(questionId, "첫 번째 답변");
        Long answerId2 = createAnswerAndGetId(questionId, "두 번째 답변");

        // 첫 번째 답변 채택
        communityAnswerService.acceptAnswer(TEST_USER_ID, questionId, answerId1);

        // 이미 채택된 답변이 있는 상태에서 다른 답변 채택 시 예외 발생
        assertThatThrownBy(() -> communityAnswerService.acceptAnswer(TEST_USER_ID, questionId, answerId2))
                .isInstanceOf(CustomException.class);
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (1900, 1, 1, '채택취소테스트', '내용', false, false, NOW(), NOW());"
    })
    @Test
    void acceptAnswer_채택취소_정상() {
        Long questionId = createQuestionAndGetId("채택취소테스트", "내용");
        Long answerId = createAnswerAndGetId(questionId, "채택할 답변");

        // 첫 번째 채택
        var result1 = communityAnswerService.acceptAnswer(TEST_USER_ID, questionId, answerId);
        assertThat(result1.accepted()).isTrue();

        // 같은 답변을 다시 채택하면 취소됨
        var result2 = communityAnswerService.acceptAnswer(TEST_USER_ID, questionId, answerId);
        assertThat(result2.accepted()).isFalse();
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (2000, 1, 1, 'getAnswerDetails테스트', '내용', false, false, NOW(), NOW());"
    })
    @Test
    void getAnswerDetails_존재하지않는답변_예외() {
        // 존재하지 않는 답변 상세 조회 시 예외 발생
        assertThatThrownBy(() -> communityAnswerService.getAnswerDetails(99999L))
                .isInstanceOf(CustomException.class);
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (2101, 1, 1, 'getAnswerDetails첨부파일테스트', '내용', false, false, NOW(), NOW());",
            "INSERT INTO web_service.community_answers (id, user_id, question_id, content, answer_image_id, created_at, updated_at, is_deleted, is_accepted) " +
                    "VALUES (2101, 1, 2101, '첨부파일있는답변상세조회', null, NOW(), NOW(), false, false);",
            "INSERT INTO web_service.files (id, container_type, container_id, name, original_name, content_type, size, created_at, updated_at) " +
                    "VALUES (2101, 'ANSWER_IMAGE', 2101, 'answer_detail_file1.jpg', 'original_answer_detail_file1.jpg', 'image/jpeg', 2048, NOW(), NOW());",
            "INSERT INTO web_service.community_answers_attachments (id, answer_id, file_id) " +
                    "VALUES (2101, 2101, 2101);"
    })
    @Test
    void getAnswerDetails_첨부파일포함_정상() {
        Long answerId = 2101L;

        var result = communityAnswerService.getAnswerDetails(answerId);

        assertThat(result).isNotNull();
        // User 정보 검증
        assertThat(result.user()).isNotNull();
        assertThat(result.user().id()).isEqualTo(TEST_USER_ID);
        assertThat(result.user().nickname()).isEqualTo("user");
        assertThat(result.user().userType()).isNotNull();
        // 답변 정보 검증
        assertThat(result.content()).isEqualTo("첨부파일있는답변상세조회");
        assertThat(result.isAccepted()).isFalse();
        // 첨부파일 정보 검증
        assertThat(result.attachments()).hasSize(1);
        var attachment = result.attachments().get(0);
        assertThat(attachment.name()).isEqualTo("original_answer_detail_file1.jpg");
        assertThat(attachment.contentType()).isEqualTo("image/jpeg");
        assertThat(attachment.size()).isEqualTo(2048);
        // 비디오 정보 검증 (없는 경우)
        assertThat(result.videoInfo()).isNull();
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (2100, 1, 1, 'getAnswerDetails정상테스트', '내용', false, false, NOW(), NOW());"
    })
    @Test
    void getAnswerDetails_정상() {
        Long questionId = 2100L;
        Long answerId = createAnswerAndGetId(questionId, "상세 조회할 답변");

        var result = communityAnswerService.getAnswerDetails(answerId);

        assertThat(result).isNotNull();
        // User 정보 검증
        assertThat(result.user()).isNotNull();
        assertThat(result.user().id()).isEqualTo(TEST_USER_ID);
        assertThat(result.user().nickname()).isEqualTo("user");
        assertThat(result.user().userType()).isNotNull();
        // 답변 정보 검증
        assertThat(result.content()).isEqualTo("상세 조회할 답변");
        assertThat(result.isAccepted()).isFalse();
        // 첨부파일 정보 검증 (없는 경우)
        assertThat(result.attachments()).isEmpty();
        // 비디오 정보 검증 (없는 경우)
        assertThat(result.videoInfo()).isNull();
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (2200, 1, 1, '빈답변목록테스트', '내용', false, false, NOW(), NOW());"
    })
    @Test
    void getAllAnswers_ByQuestionId_빈목록_정상() {
        Long questionId = createQuestionAndGetId("빈답변목록테스트", "내용");

        var result = communityAnswerService.getAllAnswersByQuestionId(questionId);

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void getAllAnswers_ByQuestionId_존재하지않는질문_예외() {
        // 존재하지 않는 질문에 대해 답변 목록 조회 시 예외 발생
        assertThatThrownBy(() -> communityAnswerService.getAllAnswersByQuestionId(99999L))
                .isInstanceOf(CustomException.class);
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (2300, 1, 1, '삭제된질문테스트', '내용', false, true, NOW(), NOW());"
    })
    @Test
    void getAllAnswers_ByQuestionId_삭제된질문_예외() {
        // 삭제된 질문에 대해 답변 목록 조회 시 예외 발생
        assertThatThrownBy(() -> communityAnswerService.getAllAnswersByQuestionId(2300L))
                .isInstanceOf(CustomException.class);
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (2300, 1, 1, '삭제된답변제외테스트', '내용', false, false, NOW(), NOW());"
    })
    @Test
    void getAllAnswers_ByQuestionId_삭제된답변제외_정상() {
        Long questionId = createQuestionAndGetId("삭제된답변제외테스트", "내용");
        Long answerId1 = createAnswerAndGetId(questionId, "유지할 답변");
        Long answerId2 = createAnswerAndGetId(questionId, "삭제할 답변");

        // 하나의 답변 삭제
        communityAnswerService.deleteAnswer(TEST_USER_ID, answerId2);
        entityManager.flush();
        entityManager.clear();

        var result = communityAnswerService.getAllAnswersByQuestionId(questionId);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).content()).isEqualTo("유지할 답변");
    }





    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (2700, 1, 1, '채택된답변수정테스트', '내용', false, false, NOW(), NOW());"
    })
    @Test
    void updateAnswer_채택된답변수정_정상() {
        Long questionId = createQuestionAndGetId("채택된답변수정테스트", "내용");
        Long answerId = createAnswerAndGetId(questionId, "원본 답변");

        // 답변 채택
        communityAnswerService.acceptAnswer(TEST_USER_ID, questionId, answerId);

        // 채택된 답변 수정
        var updateReq = new CommunityAnswerUpdateReq("수정된 답변", null, List.of());
        var result = communityAnswerService.updateAnswer(TEST_USER_ID, answerId, updateReq, List.of());

        assertThat(result).isNotNull();
        assertThat(result.content()).isEqualTo("수정된 답변");
        assertThat(result.isAccepted()).isTrue(); // 채택 상태는 유지되어야 함
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (2800, 1, 1, '채택된답변삭제테스트', '내용', false, false, NOW(), NOW());"
    })
    @Test
    void deleteAnswer_채택된답변삭제_정상() {
        Long questionId = createQuestionAndGetId("채택된답변삭제테스트", "내용");
        Long answerId = createAnswerAndGetId(questionId, "채택된 답변");

        // 답변 채택
        communityAnswerService.acceptAnswer(TEST_USER_ID, questionId, answerId);

        // 채택된 답변 삭제
        communityAnswerService.deleteAnswer(TEST_USER_ID, answerId);
        entityManager.flush();
        entityManager.clear();

        // 삭제된 답변은 조회되지 않아야 함
        var allAnswers = communityAnswerService.getAllAnswersByQuestionId(questionId);
        assertThat(allAnswers).isEmpty();
    }



    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (3000, 1, 1, '특수문자테스트', '내용', false, false, NOW(), NOW());"
    })
    @Test
    void saveAnswer_특수문자_정상() {
        Long questionId = createQuestionAndGetId("특수문자테스트", "내용");

        // 특수문자가 포함된 내용으로 답변 작성
        String specialContent = "답변 내용에 특수문자: !@#$%^&*()_+-=[]{}|;':\",./<>?`~";
        var req = new CommunityAnswerCreateReq(questionId, specialContent, null);
        var result = communityAnswerService.saveAnswer(TEST_USER_ID, req, List.of());

        assertThat(result).isNotNull();
        assertThat(result.content()).isEqualTo(specialContent);
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (3100, 1, 1, '공백문자테스트', '내용', false, false, NOW(), NOW());"
    })
    @Test
    void saveAnswer_공백문자만_예외() {
        Long questionId = createQuestionAndGetId("공백문자테스트", "내용");

        // 공백 문자만으로 답변 작성 시 예외 발생
        var req = new CommunityAnswerCreateReq(questionId, "   \t\n   ", null);
        assertThatThrownBy(() -> communityAnswerService.saveAnswer(TEST_USER_ID, req, List.of()))
                .isInstanceOf(CustomException.class);
    }



    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (3300, 1, 1, '순서테스트', '내용', false, false, NOW(), NOW());"
    })
    @Test
    void getAllAnswers_ByQuestionId_생성순서대로정렬_정상() {
        Long questionId = createQuestionAndGetId("순서테스트", "내용");

        // 여러 답변을 순서대로 생성
        communityAnswerService.saveAnswer(TEST_USER_ID, new CommunityAnswerCreateReq(questionId,"첫 번째 답변", null), List.of());
        communityAnswerService.saveAnswer(TEST_USER_ID, new CommunityAnswerCreateReq(questionId,"두 번째 답변", null), List.of());
        communityAnswerService.saveAnswer(TEST_USER_ID, new CommunityAnswerCreateReq(questionId,"세 번째 답변", null), List.of());

        var result = communityAnswerService.getAllAnswersByQuestionId(questionId);

        assertThat(result).hasSize(3);
        // 생성 순서대로 정렬되어야 함 (실제 구현에 따라 다를 수 있음)
        assertThat(result.get(0).content()).isEqualTo("첫 번째 답변");
        assertThat(result.get(1).content()).isEqualTo("두 번째 답변");
        assertThat(result.get(2).content()).isEqualTo("세 번째 답변");
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (3400, 1, 1, '채택상태유지테스트', '내용', false, false, NOW(), NOW());"
    })
    @Test
    void updateAnswer_채택상태유지_정상() {
        Long questionId = createQuestionAndGetId("채택상태유지테스트", "내용");
        Long answerId = createAnswerAndGetId(questionId, "원본 답변");

        // 답변 채택
        communityAnswerService.acceptAnswer(TEST_USER_ID, questionId, answerId);

        // 답변 수정 후 채택 상태 확인
        var updateReq = new CommunityAnswerUpdateReq("수정된 답변", null, List.of());
        var result = communityAnswerService.updateAnswer(TEST_USER_ID, answerId, updateReq, List.of());

        assertThat(result.isAccepted()).isTrue();

        // 전체 답변 목록에서도 채택 상태 확인
        var allAnswers = communityAnswerService.getAllAnswersByQuestionId(questionId);
        var acceptedAnswer = allAnswers.stream()
                .filter(answer -> answer.content().equals("수정된 답변"))
                .findFirst()
                .orElse(null);

        assertThat(acceptedAnswer).isNotNull();
        assertThat(acceptedAnswer.isAccepted()).isTrue();
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (3500, 1, 1, '삭제된답변채택테스트', '내용', false, false, NOW(), NOW());"
    })
    @Test
    void acceptAnswer_삭제된답변채택_예외() {
        Long questionId = createQuestionAndGetId("삭제된답변채택테스트", "내용");
        Long answerId = createAnswerAndGetId(questionId, "삭제할 답변");

        // 답변 삭제
        communityAnswerService.deleteAnswer(TEST_USER_ID, answerId);
        entityManager.flush();
        entityManager.clear();

        // 삭제된 답변 채택 시 예외 발생
        assertThatThrownBy(() -> communityAnswerService.acceptAnswer(TEST_USER_ID, questionId, answerId))
                .isInstanceOf(CustomException.class);
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (8888, 1, 1, '파일10개답변질문', '내용', false, false, NOW(), NOW());"
    })
    @Test
    void saveAnswer_첨부파일_10개초과_예외() {
        Long questionId = 8888L;
        var req = new CommunityAnswerCreateReq(questionId,"답변", null);
        List<org.springframework.web.multipart.MultipartFile> files = java.util.stream.IntStream.range(0, MAX_FILE_COUNT+1)
                .mapToObj(i -> org.mockito.Mockito.mock(org.springframework.web.multipart.MultipartFile.class))
                .toList();
        files.forEach(f -> org.mockito.Mockito.when(f.isEmpty()).thenReturn(false));
        assertThatThrownBy(() -> communityAnswerService.saveAnswer(TEST_USER_ID, req, files))
                .isInstanceOf(insty.exception.CustomException.class);
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (8889, 1, 1, '파일10개답변질문2', '내용', false, false, NOW(), NOW());"
    })
    @Test
    void updateAnswer_첨부파일_10개초과_예외() {
        Long questionId = 8889L;
        Long answerId = createAnswerAndGetId(questionId, "원본 답변");
        var updateReq = new CommunityAnswerUpdateReq("수정", null, List.of());
        List<org.springframework.web.multipart.MultipartFile> files = java.util.stream.IntStream.range(0, MAX_FILE_COUNT+1)
                .mapToObj(i -> org.mockito.Mockito.mock(org.springframework.web.multipart.MultipartFile.class))
                .toList();
        files.forEach(f -> org.mockito.Mockito.when(f.isEmpty()).thenReturn(false));
        assertThatThrownBy(() -> communityAnswerService.updateAnswer(TEST_USER_ID, answerId, updateReq, files))
                .isInstanceOf(insty.exception.CustomException.class);
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (2102, 1, 1, 'getAnswerDetails삭제된답변테스트', '내용', false, false, NOW(), NOW());",
            "INSERT INTO web_service.community_answers (id, user_id, question_id, content, answer_image_id, created_at, updated_at, is_deleted, is_accepted) " +
                    "VALUES (2102, 1, 2102, '삭제된답변', null, NOW(), NOW(), true, false);"
    })
    @Test
    void getAnswerDetails_삭제된답변_예외() {
        // 삭제된 답변 상세 조회 시 예외 발생
        assertThatThrownBy(() -> communityAnswerService.getAnswerDetails(2102L))
                .isInstanceOf(CustomException.class);
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (41, 1, 1, '비디오답변질문', '비디오답변질문 내용', false, false, NOW(), NOW());",
            "INSERT INTO web_service.video_answers (id, video_uuid, original_file_name, s3key, extension, duration, encoding_status, encoding_at, user_id, community_answer_id, is_deleted, created_at, updated_at) " +
                    "VALUES (1, '00000000-0000-0000-0000-000000000001', 'test_video.mp4', 'vod/ANSWER/mp4/00000000-0000-0000-0000-000000000001/test_video.mp4', 'mp4', 0, 'COMPLETED', NOW(), 1, null, false, NOW(), NOW());"
    })
    @Test
    void saveAnswer_비디오UUID포함_정상() {
        // given
        Long questionId = 41L;
        String content = "비디오가 포함된 답변";
        UUID videoUuid = UUID.fromString("00000000-0000-0000-0000-000000000001");
        var req = new CommunityAnswerCreateReq(questionId, content, videoUuid);

        // when
        var result = communityAnswerService.saveAnswer(TEST_USER_ID, req, List.of());

        // then
        assertThat(result).isNotNull();
        assertThat(result.content()).isEqualTo(content);
        assertThat(result.videoInfo()).isNotNull(); // 비디오 정보가 생성되어야 함

        // User 정보 검증
        assertThat(result.user().id()).isEqualTo(TEST_USER_ID);
        assertThat(result.user().nickname()).isEqualTo("user");
        assertThat(result.user().userType()).isEqualTo(insty.model.user.UserType.CREATOR);

        // 기본 상태 검증
        assertThat(result.isAccepted()).isFalse();
        assertThat(result.attachments()).isEmpty();
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (42, 1, 1, '첨부파일답변질문', '첨부파일답변질문 내용', false, false, NOW(), NOW());"
    })
    @Test
    void saveAnswer_첨부파일포함_정상() {
        // given
        Long questionId = 42L;
        String content = "첨부파일이 포함된 답변";
        var req = new CommunityAnswerCreateReq(questionId, content, null);

        // Mock 첨부파일 생성
        var mockFile = org.mockito.Mockito.mock(org.springframework.web.multipart.MultipartFile.class);
        org.mockito.Mockito.when(mockFile.isEmpty()).thenReturn(false);
        org.mockito.Mockito.when(mockFile.getOriginalFilename()).thenReturn("test.jpg");
        org.mockito.Mockito.when(mockFile.getContentType()).thenReturn("image/jpeg");
        org.mockito.Mockito.when(mockFile.getSize()).thenReturn(1024L);

        List<org.springframework.web.multipart.MultipartFile> attachments = List.of(mockFile);

        // when
        var result = communityAnswerService.saveAnswer(TEST_USER_ID, req, attachments);

        // then
        assertThat(result).isNotNull();
        assertThat(result.content()).isEqualTo(content);
        assertThat(result.attachments()).isNotEmpty();
        assertThat(result.attachments()).hasSize(1);

        var attachment = result.attachments().get(0);
        assertThat(attachment.name()).isEqualTo("test.jpg");
        assertThat(attachment.contentType()).isEqualTo("image/jpeg");
        assertThat(attachment.size()).isEqualTo(1024L);

        // User 정보 검증
        assertThat(result.user().id()).isEqualTo(TEST_USER_ID);
        assertThat(result.user().nickname()).isEqualTo("user");
        assertThat(result.user().userType()).isEqualTo(insty.model.user.UserType.CREATOR);

        // 기본 상태 검증
        assertThat(result.isAccepted()).isFalse();
        assertThat(result.videoInfo()).isNull();
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (43, 1, 1, '복합답변질문', '복합답변질문 내용', false, false, NOW(), NOW());",
            "INSERT INTO web_service.video_answers (id, video_uuid, original_file_name, s3key, extension, duration, encoding_status, encoding_at, user_id, community_answer_id, is_deleted, created_at, updated_at) " +
                    "VALUES (2, '00000000-0000-0000-0000-000000000002', 'test_video2.mp4', 'vod/ANSWER/mp4/00000000-0000-0000-0000-000000000002/test_video2.mp4', 'mp4', 0, 'COMPLETED', NOW(), 1, null, false, NOW(), NOW());"
    })
    @Test
    void saveAnswer_비디오와첨부파일모두포함_정상() {
        // given
        Long questionId = 43L;
        String content = "비디오와 첨부파일이 모두 포함된 답변";
        UUID videoUuid = UUID.fromString("00000000-0000-0000-0000-000000000002");
        var req = new CommunityAnswerCreateReq(questionId, content, videoUuid);

        // Mock 첨부파일 생성
        var mockFile = org.mockito.Mockito.mock(org.springframework.web.multipart.MultipartFile.class);
        org.mockito.Mockito.when(mockFile.isEmpty()).thenReturn(false);
        org.mockito.Mockito.when(mockFile.getOriginalFilename()).thenReturn("test.png");
        org.mockito.Mockito.when(mockFile.getContentType()).thenReturn("image/png");
        org.mockito.Mockito.when(mockFile.getSize()).thenReturn(2048L);

        List<org.springframework.web.multipart.MultipartFile> attachments = List.of(mockFile);

        // when
        var result = communityAnswerService.saveAnswer(TEST_USER_ID, req, attachments);

        // then
        assertThat(result).isNotNull();
        assertThat(result.content()).isEqualTo(content);
        assertThat(result.videoInfo()).isNotNull();
        assertThat(result.attachments()).isNotEmpty();
        assertThat(result.attachments()).hasSize(1);

        var attachment = result.attachments().get(0);
        assertThat(attachment.name()).isEqualTo("test.png");
        assertThat(attachment.contentType()).isEqualTo("image/png");
        assertThat(attachment.size()).isEqualTo(2048L);

        // User 정보 검증
        assertThat(result.user().id()).isEqualTo(TEST_USER_ID);
        assertThat(result.user().nickname()).isEqualTo("user");
        assertThat(result.user().userType()).isEqualTo(insty.model.user.UserType.CREATOR);

        // 기본 상태 검증
        assertThat(result.isAccepted()).isFalse();
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (44, 1, 1, '긴내용답변질문', '긴내용답변질문 내용', false, false, NOW(), NOW());"
    })
    @Test
    void saveAnswer_긴내용_정상() {
        // given
        Long questionId = 44L;
        String longContent = "a".repeat(10000); // 매우 긴 내용
        var req = new CommunityAnswerCreateReq(questionId, longContent, null);

        // when
        var result = communityAnswerService.saveAnswer(TEST_USER_ID, req, List.of());

        // then
        assertThat(result).isNotNull();
        assertThat(result.content()).isEqualTo(longContent);
        assertThat(result.content().length()).isEqualTo(10000);

        // User 정보 검증
        assertThat(result.user().id()).isEqualTo(TEST_USER_ID);
        assertThat(result.user().nickname()).isEqualTo("user");
        assertThat(result.user().userType()).isEqualTo(insty.model.user.UserType.CREATOR);

        // 기본 상태 검증
        assertThat(result.isAccepted()).isFalse();
        assertThat(result.attachments()).isEmpty();
        assertThat(result.videoInfo()).isNull();
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (45, 1, 1, '특수문자답변질문', '특수문자답변질문 내용', false, false, NOW(), NOW());"
    })
    @Test
    void saveAnswer_특수문자포함_정상() {
        // given
        Long questionId = 45L;
        String specialContent = "답변 내용에 특수문자: !@#$%^&*()_+-=[]{}|;':\",./<>?`~ 한글도 포함";
        var req = new CommunityAnswerCreateReq(questionId, specialContent, null);

        // when
        var result = communityAnswerService.saveAnswer(TEST_USER_ID, req, List.of());

        // then
        assertThat(result).isNotNull();
        assertThat(result.content()).isEqualTo(specialContent);

        // User 정보 검증
        assertThat(result.user().id()).isEqualTo(TEST_USER_ID);
        assertThat(result.user().nickname()).isEqualTo("user");
        assertThat(result.user().userType()).isEqualTo(insty.model.user.UserType.CREATOR);

        // 기본 상태 검증
        assertThat(result.isAccepted()).isFalse();
        assertThat(result.attachments()).isEmpty();
        assertThat(result.videoInfo()).isNull();
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (46, 1, 1, '여러첨부파일답변질문', '여러첨부파일답변질문 내용', false, false, NOW(), NOW());"
    })
    @Test
    void saveAnswer_여러첨부파일_정상() {
        // given
        Long questionId = 46L;
        String content = "여러 첨부파일이 포함된 답변";
        var req = new CommunityAnswerCreateReq(questionId, content, null);

        // Mock 첨부파일들 생성
        var mockFile1 = org.mockito.Mockito.mock(org.springframework.web.multipart.MultipartFile.class);
        org.mockito.Mockito.when(mockFile1.isEmpty()).thenReturn(false);
        org.mockito.Mockito.when(mockFile1.getOriginalFilename()).thenReturn("test1.jpg");
        org.mockito.Mockito.when(mockFile1.getContentType()).thenReturn("image/jpeg");
        org.mockito.Mockito.when(mockFile1.getSize()).thenReturn(1024L);

        var mockFile2 = org.mockito.Mockito.mock(org.springframework.web.multipart.MultipartFile.class);
        org.mockito.Mockito.when(mockFile2.isEmpty()).thenReturn(false);
        org.mockito.Mockito.when(mockFile2.getOriginalFilename()).thenReturn("test2.png");
        org.mockito.Mockito.when(mockFile2.getContentType()).thenReturn("image/png");
        org.mockito.Mockito.when(mockFile2.getSize()).thenReturn(2048L);

        var mockFile3 = org.mockito.Mockito.mock(org.springframework.web.multipart.MultipartFile.class);
        org.mockito.Mockito.when(mockFile3.isEmpty()).thenReturn(false);
        org.mockito.Mockito.when(mockFile3.getOriginalFilename()).thenReturn("test3.pdf");
        org.mockito.Mockito.when(mockFile3.getContentType()).thenReturn("application/pdf");
        org.mockito.Mockito.when(mockFile3.getSize()).thenReturn(5120L);

        List<org.springframework.web.multipart.MultipartFile> attachments = List.of(mockFile1, mockFile2, mockFile3);

        // when
        var result = communityAnswerService.saveAnswer(TEST_USER_ID, req, attachments);

        // then
        assertThat(result).isNotNull();
        assertThat(result.content()).isEqualTo(content);
        assertThat(result.attachments()).hasSize(3);

        // 각 첨부파일 검증
        var fileNames = result.attachments().stream().map(insty.domain.common.FileInfo::name).toList();
        assertThat(fileNames).containsExactlyInAnyOrder("test1.jpg", "test2.png", "test3.pdf");

        var contentTypes = result.attachments().stream().map(insty.domain.common.FileInfo::contentType).toList();
        assertThat(contentTypes).containsExactlyInAnyOrder("image/jpeg", "image/png", "application/pdf");

        var sizes = result.attachments().stream().map(insty.domain.common.FileInfo::size).toList();
        assertThat(sizes).containsExactlyInAnyOrder(1024L, 2048L, 5120L);

        // User 정보 검증
        assertThat(result.user().id()).isEqualTo(TEST_USER_ID);
        assertThat(result.user().nickname()).isEqualTo("user");
        assertThat(result.user().userType()).isEqualTo(insty.model.user.UserType.CREATOR);

        // 기본 상태 검증
        assertThat(result.isAccepted()).isFalse();
        assertThat(result.videoInfo()).isNull();
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (47, 1, 1, '빈첨부파일답변질문', '빈첨부파일답변질문 내용', false, false, NOW(), NOW());"
    })
    @Test
    void saveAnswer_빈첨부파일목록_정상() {
        // given
        Long questionId = 47L;
        String content = "빈 첨부파일 목록이 있는 답변";
        var req = new CommunityAnswerCreateReq(questionId, content, null);

        // when
        var result = communityAnswerService.saveAnswer(TEST_USER_ID, req, List.of());

        // then
        assertThat(result).isNotNull();
        assertThat(result.content()).isEqualTo(content);
        assertThat(result.attachments()).isEmpty();

        // User 정보 검증
        assertThat(result.user().id()).isEqualTo(TEST_USER_ID);
        assertThat(result.user().nickname()).isEqualTo("user");
        assertThat(result.user().userType()).isEqualTo(insty.model.user.UserType.CREATOR);

        // 기본 상태 검증
        assertThat(result.isAccepted()).isFalse();
        assertThat(result.videoInfo()).isNull();
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (48, 1, 1, '동시성테스트질문', '동시성테스트질문 내용', false, false, NOW(), NOW());"
    })
    @Test
    void saveAnswer_동시에같은질문에답변작성_정상() {
        // given
        Long questionId = 48L;
        var req1 = new CommunityAnswerCreateReq(questionId, "첫 번째 답변", null);
        var req2 = new CommunityAnswerCreateReq(questionId, "두 번째 답변", null);
        var req3 = new CommunityAnswerCreateReq(questionId, "세 번째 답변", null);

        // when - 동시에 같은 질문에 답변 작성 (실제로는 순차 실행)
        var result1 = communityAnswerService.saveAnswer(TEST_USER_ID, req1, List.of());
        var result2 = communityAnswerService.saveAnswer(TEST_USER_ID, req2, List.of());
        var result3 = communityAnswerService.saveAnswer(TEST_USER_ID, req3, List.of());

        // then
        assertThat(result1).isNotNull();
        assertThat(result2).isNotNull();
        assertThat(result3).isNotNull();
        assertThat(result1.content()).isEqualTo("첫 번째 답변");
        assertThat(result2.content()).isEqualTo("두 번째 답변");
        assertThat(result3.content()).isEqualTo("세 번째 답변");

        // 모든 답변이 같은 사용자에 의해 작성되었는지 확인
        assertThat(result1.user().id()).isEqualTo(TEST_USER_ID);
        assertThat(result2.user().id()).isEqualTo(TEST_USER_ID);
        assertThat(result3.user().id()).isEqualTo(TEST_USER_ID);

        // 전체 답변 목록 확인
        var allAnswers = communityAnswerService.getAllAnswersByQuestionId(questionId);
        assertThat(allAnswers).hasSize(3);

        var contents = allAnswers.stream().map(insty.domain.community.dto.CommunityAnswerRes::content).toList();
        assertThat(contents).containsExactlyInAnyOrder("첫 번째 답변", "두 번째 답변", "세 번째 답변");
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (51, 1, 1, '비디오업데이트질문', '비디오업데이트질문 내용', false, false, NOW(), NOW());",
            "INSERT INTO web_service.video_answers (id, video_uuid, original_file_name, s3key, extension, duration, encoding_status, encoding_at, user_id, community_answer_id, is_deleted, created_at, updated_at) " +
                    "VALUES (3, '00000000-0000-0000-0000-000000000003', 'test_video3.mp4', 'vod/ANSWER/mp4/00000000-0000-0000-0000-000000000003/test_video3.mp4', 'mp4', 0, 'COMPLETED', NOW(), 1, null, false, NOW(), NOW());"
    })
    @Test
    void updateAnswer_비디오UUID업데이트_정상() {
        // given
        Long questionId = 51L;
        String content = "원본 답변";
        var req = new CommunityAnswerCreateReq(questionId, content, null);
        var originalResult = communityAnswerService.saveAnswer(TEST_USER_ID, req, List.of());
        Long answerId = communityAnswerReader.getAllCommunityAnswersByQuestionId(questionId).stream()
                .filter(a -> a.getContent().equals(content))
                .findFirst()
                .map(insty.model.community.CommunityAnswer::getId)
                .orElseThrow();

        String newContent = "비디오가 추가된 답변";
        UUID videoUuid = UUID.fromString("00000000-0000-0000-0000-000000000003");
        var updateReq = new CommunityAnswerUpdateReq(newContent, videoUuid, List.of());

        // when
        var updatedRes = communityAnswerService.updateAnswer(TEST_USER_ID, answerId, updateReq, List.of());

        // then
        assertThat(updatedRes).isNotNull();
        assertThat(updatedRes.content()).isEqualTo(newContent);
        assertThat(updatedRes.videoInfo()).isNotNull(); // 비디오 정보가 생성되어야 함

        // User 정보 검증
        assertThat(updatedRes.user().id()).isEqualTo(TEST_USER_ID);
        assertThat(updatedRes.user().nickname()).isEqualTo("user");
        assertThat(updatedRes.user().userType()).isEqualTo(insty.model.user.UserType.CREATOR);

        // 기본 상태 검증
        assertThat(updatedRes.isAccepted()).isFalse();
        assertThat(updatedRes.attachments()).isEmpty();
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (52, 1, 1, '첨부파일업데이트질문', '첨부파일업데이트질문 내용', false, false, NOW(), NOW());"
    })
    @Test
    void updateAnswer_첨부파일추가_정상() {
        // given
        Long questionId = 52L;
        String content = "원본 답변";
        var req = new CommunityAnswerCreateReq(questionId, content, null);
        var originalResult = communityAnswerService.saveAnswer(TEST_USER_ID, req, List.of());
        Long answerId = communityAnswerReader.getAllCommunityAnswersByQuestionId(questionId).stream()
                .filter(a -> a.getContent().equals(content))
                .findFirst()
                .map(insty.model.community.CommunityAnswer::getId)
                .orElseThrow();

        String newContent = "첨부파일이 추가된 답변";
        var updateReq = new CommunityAnswerUpdateReq(newContent, null, List.of());

        // Mock 첨부파일 생성
        var mockFile = org.mockito.Mockito.mock(org.springframework.web.multipart.MultipartFile.class);
        org.mockito.Mockito.when(mockFile.isEmpty()).thenReturn(false);
        org.mockito.Mockito.when(mockFile.getOriginalFilename()).thenReturn("update_test.jpg");
        org.mockito.Mockito.when(mockFile.getContentType()).thenReturn("image/jpeg");
        org.mockito.Mockito.when(mockFile.getSize()).thenReturn(2048L);

        List<org.springframework.web.multipart.MultipartFile> attachments = List.of(mockFile);

        // when
        var updatedRes = communityAnswerService.updateAnswer(TEST_USER_ID, answerId, updateReq, attachments);

        // then
        assertThat(updatedRes).isNotNull();
        assertThat(updatedRes.content()).isEqualTo(newContent);
        assertThat(updatedRes.attachments()).isNotEmpty();
        assertThat(updatedRes.attachments()).hasSize(1);

        var attachment = updatedRes.attachments().get(0);
        assertThat(attachment.name()).isEqualTo("update_test.jpg");
        assertThat(attachment.contentType()).isEqualTo("image/jpeg");
        assertThat(attachment.size()).isEqualTo(2048L);

        // User 정보 검증
        assertThat(updatedRes.user().id()).isEqualTo(TEST_USER_ID);
        assertThat(updatedRes.user().nickname()).isEqualTo("user");
        assertThat(updatedRes.user().userType()).isEqualTo(insty.model.user.UserType.CREATOR);

        // 기본 상태 검증
        assertThat(updatedRes.isAccepted()).isFalse();
        assertThat(updatedRes.videoInfo()).isNull();
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (53, 1, 1, '복합업데이트질문', '복합업데이트질문 내용', false, false, NOW(), NOW());",
            "INSERT INTO web_service.video_answers (id, video_uuid, original_file_name, s3key, extension, duration, encoding_status, encoding_at, user_id, community_answer_id, is_deleted, created_at, updated_at) " +
                    "VALUES (4, '00000000-0000-0000-0000-000000000004', 'test_video4.mp4', 'vod/ANSWER/mp4/00000000-0000-0000-0000-000000000004/test_video4.mp4', 'mp4', 0, 'COMPLETED', NOW(), 1, null, false, NOW(), NOW());"
    })
    @Test
    void updateAnswer_비디오와첨부파일모두업데이트_정상() {
        // given
        Long questionId = 53L;
        String content = "원본 답변";
        var req = new CommunityAnswerCreateReq(questionId, content, null);
        var originalResult = communityAnswerService.saveAnswer(TEST_USER_ID, req, List.of());
        Long answerId = communityAnswerReader.getAllCommunityAnswersByQuestionId(questionId).stream()
                .filter(a -> a.getContent().equals(content))
                .findFirst()
                .map(insty.model.community.CommunityAnswer::getId)
                .orElseThrow();

        String newContent = "비디오와 첨부파일이 모두 업데이트된 답변";
        UUID videoUuid = UUID.fromString("00000000-0000-0000-0000-000000000004");
        var updateReq = new CommunityAnswerUpdateReq(newContent, videoUuid, List.of());

        // Mock 첨부파일 생성
        var mockFile = org.mockito.Mockito.mock(org.springframework.web.multipart.MultipartFile.class);
        org.mockito.Mockito.when(mockFile.isEmpty()).thenReturn(false);
        org.mockito.Mockito.when(mockFile.getOriginalFilename()).thenReturn("complex_update.png");
        org.mockito.Mockito.when(mockFile.getContentType()).thenReturn("image/png");
        org.mockito.Mockito.when(mockFile.getSize()).thenReturn(4096L);

        List<org.springframework.web.multipart.MultipartFile> attachments = List.of(mockFile);

        // when
        var updatedRes = communityAnswerService.updateAnswer(TEST_USER_ID, answerId, updateReq, attachments);

        // then
        assertThat(updatedRes).isNotNull();
        assertThat(updatedRes.content()).isEqualTo(newContent);
        assertThat(updatedRes.videoInfo()).isNotNull();
        assertThat(updatedRes.attachments()).isNotEmpty();
        assertThat(updatedRes.attachments()).hasSize(1);

        var attachment = updatedRes.attachments().get(0);
        assertThat(attachment.name()).isEqualTo("complex_update.png");
        assertThat(attachment.contentType()).isEqualTo("image/png");
        assertThat(attachment.size()).isEqualTo(4096L);

        // User 정보 검증
        assertThat(updatedRes.user().id()).isEqualTo(TEST_USER_ID);
        assertThat(updatedRes.user().nickname()).isEqualTo("user");
        assertThat(updatedRes.user().userType()).isEqualTo(insty.model.user.UserType.CREATOR);

        // 기본 상태 검증
        assertThat(updatedRes.isAccepted()).isFalse();
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (54, 1, 1, '긴내용업데이트질문', '긴내용업데이트질문 내용', false, false, NOW(), NOW());"
    })
    @Test
    void updateAnswer_긴내용업데이트_정상() {
        // given
        Long questionId = 54L;
        String content = "원본 답변";
        var req = new CommunityAnswerCreateReq(questionId, content, null);
        var originalResult = communityAnswerService.saveAnswer(TEST_USER_ID, req, List.of());
        Long answerId = communityAnswerReader.getAllCommunityAnswersByQuestionId(questionId).stream()
                .filter(a -> a.getContent().equals(content))
                .findFirst()
                .map(insty.model.community.CommunityAnswer::getId)
                .orElseThrow();

        String longContent = "a".repeat(10000); // 매우 긴 내용
        var updateReq = new CommunityAnswerUpdateReq(longContent, null, List.of());

        // when
        var updatedRes = communityAnswerService.updateAnswer(TEST_USER_ID, answerId, updateReq, List.of());

        // then
        assertThat(updatedRes).isNotNull();
        assertThat(updatedRes.content()).isEqualTo(longContent);
        assertThat(updatedRes.content().length()).isEqualTo(10000);

        // User 정보 검증
        assertThat(updatedRes.user().id()).isEqualTo(TEST_USER_ID);
        assertThat(updatedRes.user().nickname()).isEqualTo("user");
        assertThat(updatedRes.user().userType()).isEqualTo(insty.model.user.UserType.CREATOR);

        // 기본 상태 검증
        assertThat(updatedRes.isAccepted()).isFalse();
        assertThat(updatedRes.attachments()).isEmpty();
        assertThat(updatedRes.videoInfo()).isNull();
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (55, 1, 1, '특수문자업데이트질문', '특수문자업데이트질문 내용', false, false, NOW(), NOW());"
    })
    @Test
    void updateAnswer_특수문자업데이트_정상() {
        // given
        Long questionId = 55L;
        String content = "원본 답변";
        var req = new CommunityAnswerCreateReq(questionId, content, null);
        var originalResult = communityAnswerService.saveAnswer(TEST_USER_ID, req, List.of());
        Long answerId = communityAnswerReader.getAllCommunityAnswersByQuestionId(questionId).stream()
                .filter(a -> a.getContent().equals(content))
                .findFirst()
                .map(insty.model.community.CommunityAnswer::getId)
                .orElseThrow();

        String specialContent = "수정된 답변에 특수문자: !@#$%^&*()_+-=[]{}|;':\",./<>?`~ 한글도 포함";
        var updateReq = new CommunityAnswerUpdateReq(specialContent, null, List.of());

        // when
        var updatedRes = communityAnswerService.updateAnswer(TEST_USER_ID, answerId, updateReq, List.of());

        // then
        assertThat(updatedRes).isNotNull();
        assertThat(updatedRes.content()).isEqualTo(specialContent);

        // User 정보 검증
        assertThat(updatedRes.user().id()).isEqualTo(TEST_USER_ID);
        assertThat(updatedRes.user().nickname()).isEqualTo("user");
        assertThat(updatedRes.user().userType()).isEqualTo(insty.model.user.UserType.CREATOR);

        // 기본 상태 검증
        assertThat(updatedRes.isAccepted()).isFalse();
        assertThat(updatedRes.attachments()).isEmpty();
        assertThat(updatedRes.videoInfo()).isNull();
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (56, 1, 1, '권한검증질문', '권한검증질문 내용', false, false, NOW(), NOW());"
    })
    @Test
    void updateAnswer_다른사용자답변수정_예외() {
        // given
        Long questionId = 56L;
        String content = "원본 답변";
        var req = new CommunityAnswerCreateReq(questionId, content, null);
        var originalResult = communityAnswerService.saveAnswer(TEST_USER_ID, req, List.of());
        Long answerId = communityAnswerReader.getAllCommunityAnswersByQuestionId(questionId).stream()
                .filter(a -> a.getContent().equals(content))
                .findFirst()
                .map(insty.model.community.CommunityAnswer::getId)
                .orElseThrow();

        String newContent = "다른 사용자가 수정하려는 답변";
        var updateReq = new CommunityAnswerUpdateReq(newContent, null, List.of());
        Long differentUserId = 999L; // 존재하지 않는 다른 사용자 ID

        // when & then - 다른 사용자가 답변을 수정하려고 시도하면 예외 발생
        assertThatThrownBy(() -> communityAnswerService.updateAnswer(differentUserId, answerId, updateReq, List.of()))
                .isInstanceOf(CustomException.class);
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (57, 1, 1, '동일내용업데이트질문', '동일내용업데이트질문 내용', false, false, NOW(), NOW());"
    })
    @Test
    void updateAnswer_동일내용으로업데이트_정상() {
        // given
        Long questionId = 57L;
        String content = "원본 답변";
        var req = new CommunityAnswerCreateReq(questionId, content, null);
        var originalResult = communityAnswerService.saveAnswer(TEST_USER_ID, req, List.of());
        Long answerId = communityAnswerReader.getAllCommunityAnswersByQuestionId(questionId).stream()
                .filter(a -> a.getContent().equals(content))
                .findFirst()
                .map(insty.model.community.CommunityAnswer::getId)
                .orElseThrow();

        // 동일한 내용으로 업데이트
        var updateReq = new CommunityAnswerUpdateReq(content, null, List.of());

        // when
        var updatedRes = communityAnswerService.updateAnswer(TEST_USER_ID, answerId, updateReq, List.of());

        // then
        assertThat(updatedRes).isNotNull();
        assertThat(updatedRes.content()).isEqualTo(content);

        // User 정보 검증
        assertThat(updatedRes.user().id()).isEqualTo(TEST_USER_ID);
        assertThat(updatedRes.user().nickname()).isEqualTo("user");
        assertThat(updatedRes.user().userType()).isEqualTo(insty.model.user.UserType.CREATOR);

        // 기본 상태 검증
        assertThat(updatedRes.isAccepted()).isFalse();
        assertThat(updatedRes.attachments()).isEmpty();
        assertThat(updatedRes.videoInfo()).isNull();

        // 시간 정보 검증 - 업데이트 시간이 변경되었는지 확인
        assertThat(updatedRes.createdAt()).isNotNull();
        assertThat(updatedRes.updatedAt()).isNotNull();
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (58, 1, 1, '여러번업데이트질문', '여러번업데이트질문 내용', false, false, NOW(), NOW());"
    })
    @Test
    void updateAnswer_여러번업데이트_정상() {
        // given
        Long questionId = 58L;
        String content = "원본 답변";
        var req = new CommunityAnswerCreateReq(questionId, content, null);
        var originalResult = communityAnswerService.saveAnswer(TEST_USER_ID, req, List.of());
        Long answerId = communityAnswerReader.getAllCommunityAnswersByQuestionId(questionId).stream()
                .filter(a -> a.getContent().equals(content))
                .findFirst()
                .map(insty.model.community.CommunityAnswer::getId)
                .orElseThrow();

        // 첫 번째 업데이트
        String firstUpdate = "첫 번째 수정";
        var updateReq1 = new CommunityAnswerUpdateReq(firstUpdate, null, List.of());
        var result1 = communityAnswerService.updateAnswer(TEST_USER_ID, answerId, updateReq1, List.of());

        // 두 번째 업데이트
        String secondUpdate = "두 번째 수정";
        var updateReq2 = new CommunityAnswerUpdateReq(secondUpdate, null, List.of());
        var result2 = communityAnswerService.updateAnswer(TEST_USER_ID, answerId, updateReq2, List.of());

        // 세 번째 업데이트
        String thirdUpdate = "세 번째 수정";
        var updateReq3 = new CommunityAnswerUpdateReq( thirdUpdate, null, List.of());
        var result3 = communityAnswerService.updateAnswer(TEST_USER_ID, answerId, updateReq3, List.of());

        // then
        assertThat(result1).isNotNull();
        assertThat(result1.content()).isEqualTo(firstUpdate);

        assertThat(result2).isNotNull();
        assertThat(result2.content()).isEqualTo(secondUpdate);

        assertThat(result3).isNotNull();
        assertThat(result3.content()).isEqualTo(thirdUpdate);

        // 모든 결과가 같은 사용자에 의해 수정되었는지 확인
        assertThat(result1.user().id()).isEqualTo(TEST_USER_ID);
        assertThat(result2.user().id()).isEqualTo(TEST_USER_ID);
        assertThat(result3.user().id()).isEqualTo(TEST_USER_ID);

        // 시간 정보 검증 - 업데이트 시간이 순차적으로 증가하는지 확인
        assertThat(result1.updatedAt()).isBeforeOrEqualTo(result2.updatedAt());
        assertThat(result2.updatedAt()).isBeforeOrEqualTo(result3.updatedAt());
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (59, 1, 1, '빈내용업데이트예외질문', '빈내용업데이트예외질문 내용', false, false, NOW(), NOW());"
    })
    @Test
    void updateAnswer_빈내용_예외() {
        // given
        Long questionId = 59L;
        String content = "원본 답변";
        var req = new CommunityAnswerCreateReq(questionId, content, null);
        var originalResult = communityAnswerService.saveAnswer(TEST_USER_ID, req, List.of());
        Long answerId = communityAnswerReader.getAllCommunityAnswersByQuestionId(questionId).stream()
                .filter(a -> a.getContent().equals(content))
                .findFirst()
                .map(insty.model.community.CommunityAnswer::getId)
                .orElseThrow();

        // when & then - 빈 내용으로 수정 시 예외 발생
        var updateReq = new CommunityAnswerUpdateReq("", null, List.of());
        assertThatThrownBy(() -> communityAnswerService.updateAnswer(TEST_USER_ID, answerId, updateReq, List.of()))
                .isInstanceOf(CustomException.class);
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (60, 1, 1, 'null내용업데이트예외질문', 'null내용업데이트예외질문 내용', false, false, NOW(), NOW());"
    })
    @Test
    void updateAnswer_null내용_예외() {
        // given
        Long questionId = 60L;
        String content = "원본 답변";
        var req = new CommunityAnswerCreateReq(questionId, content, null);
        var originalResult = communityAnswerService.saveAnswer(TEST_USER_ID, req, List.of());
        Long answerId = communityAnswerReader.getAllCommunityAnswersByQuestionId(questionId).stream()
                .filter(a -> a.getContent().equals(content))
                .findFirst()
                .map(insty.model.community.CommunityAnswer::getId)
                .orElseThrow();

        // when & then - null 내용으로 수정 시 예외 발생
        var updateReq = new CommunityAnswerUpdateReq(null, null, List.of());
        assertThatThrownBy(() -> communityAnswerService.updateAnswer(TEST_USER_ID, answerId, updateReq, List.of()))
                .isInstanceOf(CustomException.class);
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (62, 1, 1, '공백문자업데이트예외질문', '공백문자업데이트예외질문 내용', false, false, NOW(), NOW());"
    })
    @Test
    void updateAnswer_공백문자만_예외() {
        // given
        Long questionId = 62L;
        String content = "원본 답변";
        var req = new CommunityAnswerCreateReq(questionId, content, null);
        var originalResult = communityAnswerService.saveAnswer(TEST_USER_ID, req, List.of());
        Long answerId = communityAnswerReader.getAllCommunityAnswersByQuestionId(questionId).stream()
                .filter(a -> a.getContent().equals(content))
                .findFirst()
                .map(insty.model.community.CommunityAnswer::getId)
                .orElseThrow();

        // when & then - 공백 문자만으로 수정 시 예외 발생
        var updateReq = new CommunityAnswerUpdateReq( "   \t\n   ", null, List.of());
        assertThatThrownBy(() -> communityAnswerService.updateAnswer(TEST_USER_ID, answerId, updateReq, List.of()))
                .isInstanceOf(CustomException.class);
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (63, 1, 1, 'userId누락예외질문', 'userId누락예외질문 내용', false, false, NOW(), NOW());"
    })
    @Test
    void updateAnswer_userId누락_예외() {
        // given
        Long questionId = 63L;
        String content = "원본 답변";
        var req = new CommunityAnswerCreateReq(questionId, content, null);
        var originalResult = communityAnswerService.saveAnswer(TEST_USER_ID, req, List.of());
        Long answerId = communityAnswerReader.getAllCommunityAnswersByQuestionId(questionId).stream()
                .filter(a -> a.getContent().equals(content))
                .findFirst()
                .map(insty.model.community.CommunityAnswer::getId)
                .orElseThrow();

        // when & then - userId가 null인 경우 예외 발생
        var updateReq = new CommunityAnswerUpdateReq( "수정된 내용", null, List.of());
        assertThatThrownBy(() -> communityAnswerService.updateAnswer(null, answerId, updateReq, List.of()))
                .isInstanceOf(CustomException.class);
    }
}