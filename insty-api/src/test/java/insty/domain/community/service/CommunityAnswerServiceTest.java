package insty.domain.community.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import insty.ai.adapter.AiRequester;
import insty.cloudfront.adapter.CloudFrontSigner;
import insty.domain.community.dto.CommunityAnswerCreateReq;
import insty.domain.community.dto.CommunityAnswerUpdateReq;
import insty.domain.community.implement.CommunityAnswerReader;
import insty.domain.community.implement.CommunityAnswerWriter;
import insty.domain.community.implement.CommunityQuestionReader;
import insty.domain.community.repository.CommunityAnswerRepository;
import insty.domain.community.repository.CommunityQuestionRepository;
import insty.domain.user.implement.UserReader;
import insty.domain.video.repository.VideoAnswerRepository;
import insty.exception.CustomException;
import insty.global.property.AppProperties;
import insty.model.community.CommunityAnswer;
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
    @Autowired
    private VideoAnswerRepository videoAnswerRepository;

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
    private static final int MAX_FILE_COUNT = 1;

    private Long createQuestionAndGetId(String title, String content) {
        var req = new insty.domain.community.dto.CommunityQuestionCreateReq(TEST_COURSE_ID, title, content, null);
        communityQuestionService.saveQuestion(TEST_USER_ID, req, null);
        return communityQuestionReader.getAllCommunityQuestionsByCourseId(TEST_COURSE_ID).stream()
                .filter(q -> q.getTitle().equals(title))
                .findFirst()
                .map(insty.model.community.CommunityQuestion::getId)
                .orElseThrow();
    }
    private Long createAnswerAndGetId(Long questionId, String content) {
        communityAnswerService.saveAnswer(TEST_USER_ID, questionId, new CommunityAnswerCreateReq( content, null), null);
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
        var req = CommunityAnswerCreateReq.builder()
                .content(content)
                .videoUuid(null)
                .build();

        // when
        var result = communityAnswerService.saveAnswer(TEST_USER_ID, questionId, req, List.of());

        // then
        assertThat(result).isNotNull();
        assertThat(result.user()).isNotNull();
        assertThat(result.user().id()).isEqualTo(TEST_USER_ID);
        assertThat(result.user().nickname()).isEqualTo("user");
        assertThat(result.user().userType()).isEqualTo(insty.model.user.UserType.CREATOR);
        assertThat(result.content()).isEqualTo(content);
        assertThat(result.isAccepted()).isFalse();
        assertThat(result.attachments()).isEmpty();
        assertThat(result.videoInfo()).isNull();
        assertThat(result.createdAt()).isNotNull();
        assertThat(result.updatedAt()).isNotNull();
        assertThat(result.createdAt()).isEqualTo(result.updatedAt()); // 생성 시에는 같아야 함

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
                    "VALUES (50, 1, 1, '답변수정 질문', '답변수정 질문 내용', false, false, NOW(), NOW());",
            "INSERT INTO web_service.video_answers (id, video_uuid, original_file_name, s3key, extension, duration, encoding_status, encoding_at, user_id, community_answer_id, is_deleted, created_at, updated_at) " +
                    "VALUES (5, '550e8400-e29b-41d4-a716-446655440005', 'existing_answer_video.mp4', 'vod/ANSWER/mp4/550e8400-e29b-41d4-a716-446655440005/existing_answer_video.mp4', 'mp4', 120, 'COMPLETED', NOW(), 1, null, false, NOW(), NOW());"
    })
    @Test
    void updateAnswer_정상() {
        // given
        Long questionId = 50L;
        String content = "원본 답변";
        UUID videoUuid = UUID.fromString("550e8400-e29b-41d4-a716-446655440005");
        var req = new CommunityAnswerCreateReq( content, videoUuid);
        var originalResult = communityAnswerService.saveAnswer(TEST_USER_ID, questionId, req, null);
        Long answerId = communityAnswerReader.getAllCommunityAnswersByQuestionId(questionId).stream()
                .filter(a -> a.getContent().equals(content))
                .findFirst()
                .map(insty.model.community.CommunityAnswer::getId)
                .orElseThrow();

        String newContent = "수정된 답변";
        var updateReq = new CommunityAnswerUpdateReq(newContent, null, List.of());

        // when
        var updatedRes = communityAnswerService.updateAnswer(TEST_USER_ID, answerId, updateReq, null);

        // then
        assertThat(updatedRes).isNotNull();
        assertThat(updatedRes.user()).isNotNull();
        assertThat(updatedRes.user().id()).isEqualTo(TEST_USER_ID);
        assertThat(updatedRes.user().nickname()).isEqualTo("user");
        assertThat(updatedRes.user().userType()).isEqualTo(insty.model.user.UserType.CREATOR);
        assertThat(updatedRes.content()).isEqualTo(newContent);
        assertThat(updatedRes.isAccepted()).isFalse();
        assertThat(updatedRes.attachments()).isEmpty();
        assertThat(updatedRes.videoInfo()).isNotNull(); // 기존 비디오가 유지되어야 함
        assertThat(updatedRes.videoInfo().videoUuid()).isEqualTo(videoUuid);
        assertThat(updatedRes.createdAt()).isNotNull();
        assertThat(updatedRes.updatedAt()).isNotNull();
        assertThat(updatedRes.updatedAt()).isAfterOrEqualTo(updatedRes.createdAt());

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
                    "VALUES (60, 1, 1, '답변삭제 질문', '답변삭제 질문 내용', false, false, NOW(), NOW());",
            "INSERT INTO web_service.files (id, container_type, container_id, name, original_name, content_type, size, created_at, updated_at) " +
                    "VALUES (300, 'ANSWER_IMAGE', 1, 'delete_test_file1.jpg', 'original_delete_test_file1.jpg', 'image/jpeg', 1024, NOW(), NOW());",
            "INSERT INTO web_service.files (id, container_type, container_id, name, original_name, content_type, size, created_at, updated_at) " +
                    "VALUES (301, 'ANSWER_IMAGE', 1, 'delete_test_file2.png', 'original_delete_test_file2.png', 'image/png', 2048, NOW(), NOW());"
    })
    @Test
    void deleteAnswer_첨부파일포함_정상() {
        Long questionId = createQuestionAndGetId("답변삭제 질문", "답변삭제 질문 내용");
        String content = "삭제할 답변";
        Long answerId = createAnswerAndGetId(questionId, content);

        communityAnswerService.deleteAnswer(TEST_USER_ID, answerId);
        entityManager.flush();
        entityManager.clear();

        Optional<CommunityAnswer> answerOpt = communityAnswerRepository.findById(answerId);
        assertThat(answerOpt).isPresent();
        assertThat(answerOpt.get().isDeleted()).isTrue();
        assertThat(answerOpt.get().getAttachments()).isEmpty();
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (61, 1, 1, '일반답변삭제 질문', '일반답변삭제 질문 내용', false, false, NOW(), NOW());"
    })
    @Test
    void deleteAnswer_일반답변_정상() {
        Long questionId = createQuestionAndGetId("일반답변삭제 질문", "일반답변삭제 질문 내용");
        String content = "삭제할 일반 답변";
        Long answerId = createAnswerAndGetId(questionId, content);

        communityAnswerService.deleteAnswer(TEST_USER_ID, answerId);
        entityManager.flush();
        entityManager.clear();

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
                    "VALUES (62, 1, 1, '비디오답변삭제 질문', '비디오답변삭제 질문 내용', false, false, NOW(), NOW());",
            "INSERT INTO web_service.video_answers (id, video_uuid, original_file_name, s3key, extension, duration, encoding_status, encoding_at, user_id, community_answer_id, is_deleted, created_at, updated_at) " +
                    "VALUES (15, 'ff0e8400-e29b-41d4-a716-446655440015', 'delete_video_answer.mp4', 'vod/ANSWER/mp4/ff0e8400-e29b-41d4-a716-446655440015/delete_video_answer.mp4', 'mp4', 120, 'COMPLETED', NOW(), 1, null, false, NOW(), NOW());"
    })
    @Test
    void deleteAnswer_비디오포함_정상() {
        Long questionId = createQuestionAndGetId("비디오답변삭제 질문", "비디오답변삭제 질문 내용");
        String content = "삭제할 비디오 답변";
        UUID videoUuid = UUID.fromString("ff0e8400-e29b-41d4-a716-446655440015");
        var req = new CommunityAnswerCreateReq( content, videoUuid);
        communityAnswerService.saveAnswer(TEST_USER_ID, questionId, req, null);
        Long answerId = communityAnswerReader.getAllCommunityAnswersByQuestionId(questionId).stream()
                .filter(a -> a.getContent().equals(content))
                .findFirst()
                .map(insty.model.community.CommunityAnswer::getId)
                .orElseThrow();

        communityAnswerService.deleteAnswer(TEST_USER_ID, answerId);
        entityManager.flush();
        entityManager.clear();

        Optional<CommunityAnswer> answerOpt = communityAnswerRepository.findById(answerId);
        assertThat(answerOpt).isPresent();
        assertThat(answerOpt.get().isDeleted()).isTrue();

        assertThat(answerOpt.get().getAnswerImage()).isNull();
        assertThat(answerOpt.get().getAttachments()).isEmpty();

        var videoAnswer = videoAnswerRepository.findByCommunityAnswerIdAndIsDeleted(answerId, false);
        assertThat(videoAnswer).isEmpty();

        var deletedVideoAnswer = videoAnswerRepository.findByCommunityAnswerIdAndIsDeleted(answerId, true);
        assertThat(deletedVideoAnswer).isPresent();
        assertThat(deletedVideoAnswer.get().isDeleted()).isTrue();
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

        communityAnswerService.deleteAnswer(TEST_USER_ID, answerId);
        entityManager.flush();
        entityManager.clear();

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
        var req = new CommunityAnswerCreateReq(null, null);
        assertThatThrownBy(() -> communityAnswerService.saveAnswer(TEST_USER_ID, questionId, req, List.of()))
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
        var req = new CommunityAnswerCreateReq( content, null);
        var res = communityAnswerService.saveAnswer(TEST_USER_ID, questionId, req, List.of());
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
        communityAnswerService.saveAnswer(userId, questionId, new CommunityAnswerCreateReq( "답변1", null), List.of());
        communityAnswerService.saveAnswer(userId, questionId, new CommunityAnswerCreateReq( "답변2", null), List.of());

        var res = communityAnswerService.getAllAnswersByQuestionId(questionId);

        assertThat(res).isNotNull();
        assertThat(res).hasSize(2);

        var answer1 = res.stream().filter(a -> a.content().equals("답변1")).findFirst().orElse(null);
        var answer2 = res.stream().filter(a -> a.content().equals("답변2")).findFirst().orElse(null);

        assertThat(answer1).isNotNull();
        assertThat(answer2).isNotNull();
        assertThat(answer1.user()).isNotNull();
        assertThat(answer1.user().id()).isEqualTo(userId);
        assertThat(answer1.user().nickname()).isEqualTo("user");
        assertThat(answer2.user()).isNotNull();
        assertThat(answer2.user().id()).isEqualTo(userId);
        assertThat(answer2.user().nickname()).isEqualTo("user");
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
            "INSERT INTO web_service.community_answers_files (answer_id, file_id, created_at, updated_at) " +
                    "VALUES (1, 200, NOW(), NOW());"
    })
    @Test
    void getAllAnswers_ByQuestionId_첨부파일포함_정상() {
        Long questionId = 650L;

        var res = communityAnswerService.getAllAnswersByQuestionId(questionId);

        assertThat(res).isNotNull();
        assertThat(res).hasSize(1);

        var answer = res.get(0);
        assertThat(answer.content()).isEqualTo("첨부파일있는답변");
        assertThat(answer.user()).isNotNull();
        assertThat(answer.user().id()).isEqualTo(TEST_USER_ID);
        assertThat(answer.user().nickname()).isEqualTo("user");
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
        long questionId = 99999L;
        var req = new CommunityAnswerCreateReq("답변 내용", null);
        assertThatThrownBy(() -> communityAnswerService.saveAnswer(TEST_USER_ID, questionId, req, List.of()))
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
        long questionId = 800L;
        var req = new CommunityAnswerCreateReq( "답변 내용", null);
        assertThatThrownBy(() -> communityAnswerService.saveAnswer(TEST_USER_ID, questionId, req, List.of()))
                .isInstanceOf(CustomException.class);
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
        long questionId = 1000L;
        var req = new CommunityAnswerCreateReq("", null);
        assertThatThrownBy(() -> communityAnswerService.saveAnswer(TEST_USER_ID, questionId, req, List.of()))
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
            "INSERT INTO web_service.community_answers_files (answer_id, file_id, created_at, updated_at) " +
                    "VALUES (2101, 2101, NOW(), NOW());"
    })
    @Test
    void getAnswerDetails_첨부파일포함_정상() {
        Long answerId = 2101L;

        var result = communityAnswerService.getAnswerDetails(answerId);

        assertThat(result).isNotNull();
        assertThat(result.user()).isNotNull();
        assertThat(result.user().id()).isEqualTo(TEST_USER_ID);
        assertThat(result.user().nickname()).isEqualTo("user");
        assertThat(result.user().userType()).isNotNull();
        assertThat(result.content()).isEqualTo("첨부파일있는답변상세조회");
        assertThat(result.isAccepted()).isFalse();
        assertThat(result.attachments()).hasSize(1);
        var attachment = result.attachments().get(0);
        assertThat(attachment.name()).isEqualTo("original_answer_detail_file1.jpg");
        assertThat(attachment.contentType()).isEqualTo("image/jpeg");
        assertThat(attachment.size()).isEqualTo(2048);
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
        assertThat(result.user()).isNotNull();
        assertThat(result.user().id()).isEqualTo(TEST_USER_ID);
        assertThat(result.user().nickname()).isEqualTo("user");
        assertThat(result.user().userType()).isNotNull();
        assertThat(result.content()).isEqualTo("상세 조회할 답변");
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
                    "VALUES (2700, 1, 1, '채택된답변수정테스트', '내용', false, false, NOW(), NOW());",
            "INSERT INTO web_service.video_answers (id, video_uuid, original_file_name, s3key, extension, duration, encoding_status, encoding_at, user_id, community_answer_id, is_deleted, created_at, updated_at) " +
                    "VALUES (6, '660e8400-e29b-41d4-a716-446655440006', 'accepted_answer_video.mp4', 'vod/ANSWER/mp4/660e8400-e29b-41d4-a716-446655440006/accepted_answer_video.mp4', 'mp4', 120, 'COMPLETED', NOW(), 1, null, false, NOW(), NOW());"
    })
    @Test
    void updateAnswer_채택된답변수정_정상() {
        Long questionId = createQuestionAndGetId("채택된답변수정테스트", "내용");
        UUID videoUuid = UUID.fromString("660e8400-e29b-41d4-a716-446655440006");
        var req = new CommunityAnswerCreateReq( "원본 답변", videoUuid);
        var originalResult = communityAnswerService.saveAnswer(TEST_USER_ID, questionId, req, null);
        Long answerId = communityAnswerReader.getAllCommunityAnswersByQuestionId(questionId).stream()
                .filter(a -> a.getContent().equals("원본 답변"))
                .findFirst()
                .map(insty.model.community.CommunityAnswer::getId)
                .orElseThrow();

        // 답변 채택
        communityAnswerService.acceptAnswer(TEST_USER_ID, questionId, answerId);

        // 채택된 답변 수정
        var updateReq = new CommunityAnswerUpdateReq("수정된 답변", null, null);
        var result = communityAnswerService.updateAnswer(TEST_USER_ID, answerId, updateReq, null);

        assertThat(result).isNotNull();
        assertThat(result.content()).isEqualTo("수정된 답변");
        assertThat(result.isAccepted()).isTrue(); // 채택 상태는 유지되어야 함
        assertThat(result.videoInfo()).isNotNull(); // 기존 비디오가 유지되어야 함
        assertThat(result.videoInfo().videoUuid()).isEqualTo(videoUuid);
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
        var req = new CommunityAnswerCreateReq( specialContent, null);
        var result = communityAnswerService.saveAnswer(TEST_USER_ID, questionId, req, List.of());

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
        var req = new CommunityAnswerCreateReq( "   \t\n   ", null);
        assertThatThrownBy(() -> communityAnswerService.saveAnswer(TEST_USER_ID, questionId, req, List.of()))
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
        communityAnswerService.saveAnswer(TEST_USER_ID, questionId, new CommunityAnswerCreateReq("첫 번째 답변", null), List.of());
        communityAnswerService.saveAnswer(TEST_USER_ID, questionId, new CommunityAnswerCreateReq("두 번째 답변", null), List.of());
        communityAnswerService.saveAnswer(TEST_USER_ID, questionId, new CommunityAnswerCreateReq("세 번째 답변", null), List.of());

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
                    "VALUES (3400, 1, 1, '채택상태유지테스트', '내용', false, false, NOW(), NOW());",
            "INSERT INTO web_service.video_answers (id, video_uuid, original_file_name, s3key, extension, duration, encoding_status, encoding_at, user_id, community_answer_id, is_deleted, created_at, updated_at) " +
                    "VALUES (7, '770e8400-e29b-41d4-a716-446655440007', 'accepted_status_video.mp4', 'vod/ANSWER/mp4/770e8400-e29b-41d4-a716-446655440007/accepted_status_video.mp4', 'mp4', 120, 'COMPLETED', NOW(), 1, null, false, NOW(), NOW());"
    })
    @Test
    void updateAnswer_채택상태유지_정상() {
        Long questionId = createQuestionAndGetId("채택상태유지테스트", "내용");
        UUID videoUuid = UUID.fromString("770e8400-e29b-41d4-a716-446655440007");
        var req = new CommunityAnswerCreateReq( "원본 답변", videoUuid);
        var originalResult = communityAnswerService.saveAnswer(TEST_USER_ID, questionId, req, null);
        Long answerId = communityAnswerReader.getAllCommunityAnswersByQuestionId(questionId).stream()
                .filter(a -> a.getContent().equals("원본 답변"))
                .findFirst()
                .map(insty.model.community.CommunityAnswer::getId)
                .orElseThrow();

        // 답변 채택
        communityAnswerService.acceptAnswer(TEST_USER_ID, questionId, answerId);

        // 답변 수정 후 채택 상태 확인
        var updateReq = new CommunityAnswerUpdateReq("수정된 답변", null, null);
        var result = communityAnswerService.updateAnswer(TEST_USER_ID, answerId, updateReq, null);

        assertThat(result.isAccepted()).isTrue();
        assertThat(result.videoInfo()).isNotNull(); // 기존 비디오가 유지되어야 함
        assertThat(result.videoInfo().videoUuid()).isEqualTo(videoUuid);

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
        var req = new CommunityAnswerCreateReq("답변", null);
        List<org.springframework.web.multipart.MultipartFile> files = java.util.stream.IntStream.range(0, MAX_FILE_COUNT+1)
                .mapToObj(i -> org.mockito.Mockito.mock(org.springframework.web.multipart.MultipartFile.class))
                .toList();
        files.forEach(f -> org.mockito.Mockito.when(f.isEmpty()).thenReturn(false));
        assertThatThrownBy(() -> communityAnswerService.saveAnswer(TEST_USER_ID, questionId, req, files))
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
        var req = new CommunityAnswerCreateReq( content, videoUuid);

        // when
        var result = communityAnswerService.saveAnswer(TEST_USER_ID, questionId, req, List.of());

        // then
        assertThat(result).isNotNull();
        assertThat(result.content()).isEqualTo(content);
        assertThat(result.videoInfo()).isNotNull();

        assertThat(result.user().id()).isEqualTo(TEST_USER_ID);
        assertThat(result.user().nickname()).isEqualTo("user");
        assertThat(result.user().userType()).isEqualTo(insty.model.user.UserType.CREATOR);
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
        var req = new CommunityAnswerCreateReq( content, null);

        // Mock 첨부파일 생성
        var mockFile = org.mockito.Mockito.mock(org.springframework.web.multipart.MultipartFile.class);
        org.mockito.Mockito.when(mockFile.isEmpty()).thenReturn(false);
        org.mockito.Mockito.when(mockFile.getOriginalFilename()).thenReturn("test.jpg");
        org.mockito.Mockito.when(mockFile.getContentType()).thenReturn("image/jpeg");
        org.mockito.Mockito.when(mockFile.getSize()).thenReturn(1024L);

        List<org.springframework.web.multipart.MultipartFile> attachments = List.of(mockFile);

        // when
        var result = communityAnswerService.saveAnswer(TEST_USER_ID, questionId, req, attachments);

        // then
        assertThat(result).isNotNull();
        assertThat(result.content()).isEqualTo(content);
        assertThat(result.attachments()).isNotEmpty();
        assertThat(result.attachments()).hasSize(1);

        var attachment = result.attachments().get(0);
        assertThat(attachment.name()).isEqualTo("test.jpg");
        assertThat(attachment.contentType()).isEqualTo("image/jpeg");
        assertThat(attachment.size()).isEqualTo(1024L);
        assertThat(result.user().id()).isEqualTo(TEST_USER_ID);
        assertThat(result.user().nickname()).isEqualTo("user");
        assertThat(result.user().userType()).isEqualTo(insty.model.user.UserType.CREATOR);
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
        var req = new CommunityAnswerCreateReq( content, videoUuid);

        // Mock 첨부파일 생성
        var mockFile = org.mockito.Mockito.mock(org.springframework.web.multipart.MultipartFile.class);
        org.mockito.Mockito.when(mockFile.isEmpty()).thenReturn(false);
        org.mockito.Mockito.when(mockFile.getOriginalFilename()).thenReturn("test.png");
        org.mockito.Mockito.when(mockFile.getContentType()).thenReturn("image/png");
        org.mockito.Mockito.when(mockFile.getSize()).thenReturn(2048L);

        List<org.springframework.web.multipart.MultipartFile> attachments = List.of(mockFile);

        // when
        var result = communityAnswerService.saveAnswer(TEST_USER_ID, questionId, req, attachments);

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
        assertThat(result.user().id()).isEqualTo(TEST_USER_ID);
        assertThat(result.user().nickname()).isEqualTo("user");
        assertThat(result.user().userType()).isEqualTo(insty.model.user.UserType.CREATOR);
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
        var req = new CommunityAnswerCreateReq( longContent, null);

        // when
        var result = communityAnswerService.saveAnswer(TEST_USER_ID, questionId, req, List.of());

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
        var req = new CommunityAnswerCreateReq( specialContent, null);

        // when
        var result = communityAnswerService.saveAnswer(TEST_USER_ID, questionId, req, List.of());

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
        var req = new CommunityAnswerCreateReq( content, null);

        // MAX_ANSWER_FILE_COUNT만큼 Mock 첨부파일들 생성
        List<org.springframework.web.multipart.MultipartFile> attachments = new java.util.ArrayList<>();
        String[] fileExtensions = {"jpg", "png", "pdf", "doc", "txt", "zip", "mp4", "avi", "gif", "bmp", "xlsx", "pptx", "mp3", "wav", "mov"};
        String[] expectedContentTypes = {"image/jpeg", "image/png", "application/pdf", "application/msword", "text/plain", "application/zip", "video/mp4", "video/avi", "image/gif", "image/bmp", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/vnd.openxmlformats-officedocument.presentationml.presentation", "audio/mpeg", "audio/wav", "video/quicktime"};

        int maxFileCount = MAX_FILE_COUNT;

        for (int i = 0; i < maxFileCount; i++) {
            var mockFile = org.mockito.Mockito.mock(org.springframework.web.multipart.MultipartFile.class);
            org.mockito.Mockito.when(mockFile.isEmpty()).thenReturn(false);
            org.mockito.Mockito.when(mockFile.getOriginalFilename()).thenReturn("test" + (i + 1) + "." + fileExtensions[i]);
            org.mockito.Mockito.when(mockFile.getContentType()).thenReturn(expectedContentTypes[i]);
            org.mockito.Mockito.when(mockFile.getSize()).thenReturn((long) (1024 * (i + 1)));
            attachments.add(mockFile);
        }

        // when
        var result = communityAnswerService.saveAnswer(TEST_USER_ID, questionId, req, attachments);

        // then
        assertThat(result).isNotNull();
        assertThat(result.content()).isEqualTo(content);
        assertThat(result.attachments()).hasSize(maxFileCount); // MAX_ANSWER_FILE_COUNT

        var fileNames = result.attachments().stream().map(insty.domain.common.FileInfo::name).toList();
        assertThat(fileNames).hasSize(maxFileCount);
        for (int i = 0; i < maxFileCount; i++) {
            assertThat(fileNames).contains("test" + (i + 1) + "." + fileExtensions[i]);
        }

        var resultContentTypes = result.attachments().stream().map(insty.domain.common.FileInfo::contentType).toList();
        assertThat(resultContentTypes).hasSize(maxFileCount);
        for (int i = 0; i < maxFileCount; i++) {
            assertThat(resultContentTypes).contains(expectedContentTypes[i]);
        }

        var sizes = result.attachments().stream().map(insty.domain.common.FileInfo::size).toList();
        assertThat(sizes).hasSize(maxFileCount);
        for (int i = 0; i < maxFileCount; i++) {
            assertThat(sizes).contains((long) (1024 * (i + 1)));
        }

        assertThat(result.user().id()).isEqualTo(TEST_USER_ID);
        assertThat(result.user().nickname()).isEqualTo("user");
        assertThat(result.user().userType()).isEqualTo(insty.model.user.UserType.CREATOR);

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
        var req = new CommunityAnswerCreateReq( content, null);

        // when
        var result = communityAnswerService.saveAnswer(TEST_USER_ID, questionId, req, List.of());

        // then
        assertThat(result).isNotNull();
        assertThat(result.content()).isEqualTo(content);
        assertThat(result.attachments()).isEmpty();
        assertThat(result.user().id()).isEqualTo(TEST_USER_ID);
        assertThat(result.user().nickname()).isEqualTo("user");
        assertThat(result.user().userType()).isEqualTo(insty.model.user.UserType.CREATOR);
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
        var req1 = new CommunityAnswerCreateReq( "첫 번째 답변", null);
        var req2 = new CommunityAnswerCreateReq( "두 번째 답변", null);
        var req3 = new CommunityAnswerCreateReq( "세 번째 답변", null);

        // when - 동시에 같은 질문에 답변 작성 (실제로는 순차 실행)
        var result1 = communityAnswerService.saveAnswer(TEST_USER_ID, questionId, req1, List.of());
        var result2 = communityAnswerService.saveAnswer(TEST_USER_ID, questionId, req2, List.of());
        var result3 = communityAnswerService.saveAnswer(TEST_USER_ID, questionId, req3, List.of());

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
        var req = new CommunityAnswerCreateReq( content, null);
        var originalResult = communityAnswerService.saveAnswer(TEST_USER_ID, questionId, req, List.of());
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

        assertThat(updatedRes.user().id()).isEqualTo(TEST_USER_ID);
        assertThat(updatedRes.user().nickname()).isEqualTo("user");
        assertThat(updatedRes.user().userType()).isEqualTo(insty.model.user.UserType.CREATOR);

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
        var req = new CommunityAnswerCreateReq( content, null);
        var originalResult = communityAnswerService.saveAnswer(TEST_USER_ID, questionId, req, List.of());
        Long answerId = communityAnswerReader.getAllCommunityAnswersByQuestionId(questionId).stream()
                .filter(a -> a.getContent().equals(content))
                .findFirst()
                .map(insty.model.community.CommunityAnswer::getId)
                .orElseThrow();

        String newContent = "첨부파일이 추가된 답변";
        var updateReq = new CommunityAnswerUpdateReq(newContent, null, List.of());

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

        assertThat(updatedRes.user().id()).isEqualTo(TEST_USER_ID);
        assertThat(updatedRes.user().nickname()).isEqualTo("user");
        assertThat(updatedRes.user().userType()).isEqualTo(insty.model.user.UserType.CREATOR);

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
        var req = new CommunityAnswerCreateReq( content, null);
        var originalResult = communityAnswerService.saveAnswer(TEST_USER_ID, questionId, req, List.of());
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
                    "VALUES (56, 1, 1, '권한검증질문', '권한검증질문 내용', false, false, NOW(), NOW());",
            "INSERT INTO web_service.video_answers (id, video_uuid, original_file_name, s3key, extension, duration, encoding_status, encoding_at, user_id, community_answer_id, is_deleted, created_at, updated_at) " +
                    "VALUES (11, 'bb0e8400-e29b-41d4-a716-446655440011', 'permission_video.mp4', 'vod/ANSWER/mp4/bb0e8400-e29b-41d4-a716-446655440011/permission_video.mp4', 'mp4', 120, 'COMPLETED', NOW(), 1, null, false, NOW(), NOW());"
    })
    @Test
    void updateAnswer_다른사용자답변수정_예외() {
        // given
        Long questionId = 56L;
        String content = "원본 답변";
        UUID videoUuid = UUID.fromString("bb0e8400-e29b-41d4-a716-446655440011");
        var req = new CommunityAnswerCreateReq( content, videoUuid);
        var originalResult = communityAnswerService.saveAnswer(TEST_USER_ID, questionId, req, null);
        Long answerId = communityAnswerReader.getAllCommunityAnswersByQuestionId(questionId).stream()
                .filter(a -> a.getContent().equals(content))
                .findFirst()
                .map(insty.model.community.CommunityAnswer::getId)
                .orElseThrow();

        String newContent = "다른 사용자가 수정하려는 답변";
        var updateReq = new CommunityAnswerUpdateReq(newContent, null, null);
        Long differentUserId = 999L;

        // when & then - 다른 사용자가 답변을 수정하려고 시도하면 예외 발생
        assertThatThrownBy(() -> communityAnswerService.updateAnswer(differentUserId, answerId, updateReq, null))
                .isInstanceOf(CustomException.class);
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (57, 1, 1, '동일내용업데이트질문', '동일내용업데이트질문 내용', false, false, NOW(), NOW());",
            "INSERT INTO web_service.video_answers (id, video_uuid, original_file_name, s3key, extension, duration, encoding_status, encoding_at, user_id, community_answer_id, is_deleted, created_at, updated_at) " +
                    "VALUES (12, 'cc0e8400-e29b-41d4-a716-446655440012', 'same_content_video.mp4', 'vod/ANSWER/mp4/cc0e8400-e29b-41d4-a716-446655440012/same_content_video.mp4', 'mp4', 120, 'COMPLETED', NOW(), 1, null, false, NOW(), NOW());"
    })
    @Test
    void updateAnswer_동일내용으로업데이트_정상() {
        // given
        Long questionId = 57L;
        String content = "원본 답변";
        UUID videoUuid = UUID.fromString("cc0e8400-e29b-41d4-a716-446655440012");
        var req = new CommunityAnswerCreateReq( content, videoUuid);
        var originalResult = communityAnswerService.saveAnswer(TEST_USER_ID, questionId, req, null);
        Long answerId = communityAnswerReader.getAllCommunityAnswersByQuestionId(questionId).stream()
                .filter(a -> a.getContent().equals(content))
                .findFirst()
                .map(insty.model.community.CommunityAnswer::getId)
                .orElseThrow();

        // 동일한 내용으로 업데이트
        var updateReq = new CommunityAnswerUpdateReq(content, null, null);

        // when
        var updatedRes = communityAnswerService.updateAnswer(TEST_USER_ID, answerId, updateReq, null);

        // then
        assertThat(updatedRes).isNotNull();
        assertThat(updatedRes.content()).isEqualTo(content);

        assertThat(updatedRes.user().id()).isEqualTo(TEST_USER_ID);
        assertThat(updatedRes.user().nickname()).isEqualTo("user");
        assertThat(updatedRes.user().userType()).isEqualTo(insty.model.user.UserType.CREATOR);

        assertThat(updatedRes.isAccepted()).isFalse();
        assertThat(updatedRes.attachments()).isEmpty();

        // 업데이트 시간이 변경되었는지 확인
        assertThat(updatedRes.createdAt()).isNotNull();
        assertThat(updatedRes.updatedAt()).isNotNull();
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (58, 1, 1, '여러번업데이트질문', '여러번업데이트질문 내용', false, false, NOW(), NOW());",
            "INSERT INTO web_service.video_answers (id, video_uuid, original_file_name, s3key, extension, duration, encoding_status, encoding_at, user_id, community_answer_id, is_deleted, created_at, updated_at) " +
                    "VALUES (13, 'dd0e8400-e29b-41d4-a716-446655440013', 'multiple_update_video.mp4', 'vod/ANSWER/mp4/dd0e8400-e29b-41d4-a716-446655440013/multiple_update_video.mp4', 'mp4', 120, 'COMPLETED', NOW(), 1, null, false, NOW(), NOW());"
    })
    @Test
    void updateAnswer_여러번업데이트_정상() {
        // given
        Long questionId = 58L;
        String content = "원본 답변";
        UUID videoUuid = UUID.fromString("dd0e8400-e29b-41d4-a716-446655440013");
        var req = new CommunityAnswerCreateReq( content, videoUuid);
        var originalResult = communityAnswerService.saveAnswer(TEST_USER_ID, questionId, req, null);
        Long answerId = communityAnswerReader.getAllCommunityAnswersByQuestionId(questionId).stream()
                .filter(a -> a.getContent().equals(content))
                .findFirst()
                .map(insty.model.community.CommunityAnswer::getId)
                .orElseThrow();

        // 첫 번째 업데이트
        String firstUpdate = "첫 번째 수정";
        var updateReq1 = new CommunityAnswerUpdateReq(firstUpdate, null, null);
        var result1 = communityAnswerService.updateAnswer(TEST_USER_ID, answerId, updateReq1, null);

        // 두 번째 업데이트
        String secondUpdate = "두 번째 수정";
        var updateReq2 = new CommunityAnswerUpdateReq(secondUpdate, null, null);
        var result2 = communityAnswerService.updateAnswer(TEST_USER_ID, answerId, updateReq2, null);

        // 세 번째 업데이트
        String thirdUpdate = "세 번째 수정";
        var updateReq3 = new CommunityAnswerUpdateReq( thirdUpdate, null, null);
        var result3 = communityAnswerService.updateAnswer(TEST_USER_ID, answerId, updateReq3, null);

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

        // 업데이트 시간이 순차적으로 증가하는지 확인
        assertThat(result1.updatedAt()).isBeforeOrEqualTo(result2.updatedAt());
        assertThat(result2.updatedAt()).isBeforeOrEqualTo(result3.updatedAt());
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (59, 1, 1, '빈내용업데이트예외질문', '빈내용업데이트예외질문 내용', false, false, NOW(), NOW());",
            "INSERT INTO web_service.video_answers (id, video_uuid, original_file_name, s3key, extension, duration, encoding_status, encoding_at, user_id, community_answer_id, is_deleted, created_at, updated_at) " +
                    "VALUES (14, 'ee0e8400-e29b-41d4-a716-446655440014', 'empty_content_video.mp4', 'vod/ANSWER/mp4/ee0e8400-e29b-41d4-a716-446655440014/empty_content_video.mp4', 'mp4', 120, 'COMPLETED', NOW(), 1, null, false, NOW(), NOW());"
    })
    @Test
    void updateAnswer_빈내용_예외() {
        // given
        Long questionId = 59L;
        String content = "원본 답변";
        UUID videoUuid = UUID.fromString("ee0e8400-e29b-41d4-a716-446655440014");
        var req = new CommunityAnswerCreateReq( content, videoUuid);
        var originalResult = communityAnswerService.saveAnswer(TEST_USER_ID, questionId, req, null);
        Long answerId = communityAnswerReader.getAllCommunityAnswersByQuestionId(questionId).stream()
                .filter(a -> a.getContent().equals(content))
                .findFirst()
                .map(insty.model.community.CommunityAnswer::getId)
                .orElseThrow();

        // when & then - 빈 내용으로 수정 시 예외 발생
        var updateReq = new CommunityAnswerUpdateReq("", null, null);
        assertThatThrownBy(() -> communityAnswerService.updateAnswer(TEST_USER_ID, answerId, updateReq, null))
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
        var req = new CommunityAnswerCreateReq( content, null);
        var originalResult = communityAnswerService.saveAnswer(TEST_USER_ID, questionId, req, List.of());
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
        var req = new CommunityAnswerCreateReq( content, null);
        var originalResult = communityAnswerService.saveAnswer(TEST_USER_ID, questionId, req, List.of());
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
        var req = new CommunityAnswerCreateReq( content, null);
        var originalResult = communityAnswerService.saveAnswer(TEST_USER_ID, questionId, req, List.of());
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