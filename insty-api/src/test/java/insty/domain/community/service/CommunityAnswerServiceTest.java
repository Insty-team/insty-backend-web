package insty.domain.community.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import insty.domain.community.dto.CommunityAnswerCreateReq;
import insty.domain.community.dto.CommunityAnswerRes;
import insty.domain.community.dto.CommunityAnswerUpdateReq;
import insty.domain.community.implement.CommunityAnswerReader;
import insty.domain.community.implement.CommunityAnswerWriter;
import insty.domain.community.implement.CommunityQuestionReader;
import insty.domain.user.implement.UserReader;
import insty.model.community.CommunityAnswer;
import java.util.List;
import java.util.Optional;
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
        return communityAnswerReader.getAllCommunityAnswers(questionId).stream()
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
        Long questionId = createQuestionAndGetId("답변용 질문", "답변용 질문 내용");
        String content = "답변 내용";
        Long answerId = createAnswerAndGetId(questionId, content);

        var res = communityAnswerService.getAllAnswers(questionId).stream()
                .filter(a -> a.content().equals(content))
                .findFirst()
                .orElse(null);

        assertThat(res).isNotNull();
        assertThat(res.user().id()).isEqualTo(TEST_USER_ID);
        assertThat(res.content()).isEqualTo(content);
        assertThat(res.isAccepted()).isFalse();
        assertThat(res.attachments()).isEmpty();
    }

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
        Long questionId = createQuestionAndGetId("답변수정 질문", "답변수정 질문 내용");
        String content = "원본 답변";
        Long answerId = createAnswerAndGetId(questionId, content);

        String newContent = "수정된 답변";
        var updateReq = new CommunityAnswerUpdateReq(answerId, newContent, null, List.of());
        var updatedRes = communityAnswerService.updateAnswer(TEST_USER_ID, answerId, updateReq, List.of());

        assertThat(updatedRes).isNotNull();
        assertThat(updatedRes.content()).isEqualTo(newContent);
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
        var updateReq = new CommunityAnswerUpdateReq(answerId, "수정", null, List.of());
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
                    "VALUES (600, 1, 1, '답변목록질문', '내용', false, false, NOW(), NOW());"
    })
    @Test
    void getAllAnswers_정상() {
        Long questionId = 600L, userId = 1L;
        // 답변 2개 생성
        communityAnswerService.saveAnswer(userId, new CommunityAnswerCreateReq(questionId, "답변1", null), List.of());
        communityAnswerService.saveAnswer(userId, new CommunityAnswerCreateReq(questionId, "답변2", null), List.of());

        var res = communityAnswerService.getAllAnswers(questionId);
        assertThat(res).isNotNull();
        assertThat(res).hasSize(2);
        assertThat(res.get(0).content()).isIn("답변1", "답변2");
        assertThat(res.get(1).content()).isIn("답변1", "답변2");
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
        var updateReq = new CommunityAnswerUpdateReq(99999L, "수정된 내용", null, List.of());
        assertThatThrownBy(() -> communityAnswerService.updateAnswer(TEST_USER_ID, 99999L, updateReq, List.of()))
                .isInstanceOf(CustomException.class);
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (1200, 1, 1, 'updateAnswer빈내용테스트', '내용', false, false, NOW(), NOW());"
    })
    @Test
    void updateAnswer_빈내용_예외() {
        Long questionId = createQuestionAndGetId("updateAnswer빈내용테스트", "내용");
        Long answerId = createAnswerAndGetId(questionId, "원본 답변");

        // 빈 내용으로 수정 시 예외 발생
        var updateReq = new CommunityAnswerUpdateReq(answerId, "", null, List.of());
        assertThatThrownBy(() -> communityAnswerService.updateAnswer(TEST_USER_ID, answerId, updateReq, List.of()))
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
                    "VALUES (2100, 1, 1, 'getAnswerDetails정상테스트', '내용', false, false, NOW(), NOW());"
    })
    @Test
    void getAnswerDetails_정상() {
        Long questionId = createQuestionAndGetId("getAnswerDetails정상테스트", "내용");
        Long answerId = createAnswerAndGetId(questionId, "상세 조회할 답변");

        var result = communityAnswerService.getAnswerDetails(answerId);

        assertThat(result).isNotNull();
        assertThat(result.user().id()).isEqualTo(TEST_USER_ID);
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
    void getAllAnswers_빈목록_정상() {
        Long questionId = createQuestionAndGetId("빈답변목록테스트", "내용");

        var result = communityAnswerService.getAllAnswers(questionId);

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
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
    void getAllAnswers_삭제된답변제외_정상() {
        Long questionId = createQuestionAndGetId("삭제된답변제외테스트", "내용");
        Long answerId1 = createAnswerAndGetId(questionId, "유지할 답변");
        Long answerId2 = createAnswerAndGetId(questionId, "삭제할 답변");

        // 하나의 답변 삭제
        communityAnswerService.deleteAnswer(TEST_USER_ID, answerId2);
        entityManager.flush();
        entityManager.clear();

        var result = communityAnswerService.getAllAnswers(questionId);

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
                    "VALUES (2400, 1, 1, 'updateAnswer빈내용테스트', '내용', false, false, NOW(), NOW());"
    })
    @Test
    void updateAnswer_null내용_예외() {
        Long questionId = createQuestionAndGetId("updateAnswer빈내용테스트", "내용");
        Long answerId = createAnswerAndGetId(questionId, "원본 답변");

        // null 내용으로 수정 시 예외 발생
        var updateReq = new CommunityAnswerUpdateReq(answerId, null, null, List.of());
        assertThatThrownBy(() -> communityAnswerService.updateAnswer(TEST_USER_ID, answerId, updateReq, List.of()))
                .isInstanceOf(CustomException.class);
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (2500, 1, 1, 'updateAnsweranswerId누락테스트', '내용', false, false, NOW(), NOW());"
    })
    @Test
    void updateAnswer_answerId누락_예외() {
        Long questionId = createQuestionAndGetId("updateAnsweranswerId누락테스트", "내용");
        Long answerId = createAnswerAndGetId(questionId, "원본 답변");

        // answerId가 null인 경우 예외 발생
        var updateReq = new CommunityAnswerUpdateReq(null, "수정된 내용", null, List.of());
        assertThatThrownBy(() -> communityAnswerService.updateAnswer(TEST_USER_ID, answerId, updateReq, List.of()))
                .isInstanceOf(CustomException.class);
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (2600, 1, 1, '동시성테스트', '내용', false, false, NOW(), NOW());"
    })
    @Test
    void saveAnswer_동시에같은질문에답변작성_정상() {
        Long questionId = createQuestionAndGetId("동시성테스트", "내용");

        // 동시에 같은 질문에 답변 작성 (실제로는 순차 실행)
        var req1 = new CommunityAnswerCreateReq(questionId, "첫 번째 답변", null);
        var req2 = new CommunityAnswerCreateReq(questionId, "두 번째 답변", null);

        var result1 = communityAnswerService.saveAnswer(TEST_USER_ID, req1, List.of());
        var result2 = communityAnswerService.saveAnswer(TEST_USER_ID, req2, List.of());

        assertThat(result1).isNotNull();
        assertThat(result2).isNotNull();
        assertThat(result1.content()).isEqualTo("첫 번째 답변");
        assertThat(result2.content()).isEqualTo("두 번째 답변");

        var allAnswers = communityAnswerService.getAllAnswers(questionId);
        assertThat(allAnswers).hasSize(2);
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
        var updateReq = new CommunityAnswerUpdateReq(answerId, "수정된 답변", null, List.of());
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
        var allAnswers = communityAnswerService.getAllAnswers(questionId);
        assertThat(allAnswers).isEmpty();
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (2900, 1, 1, '긴내용테스트', '내용', false, false, NOW(), NOW());"
    })
    @Test
    void saveAnswer_긴내용_정상() {
        Long questionId = createQuestionAndGetId("긴내용테스트", "내용");

        // 매우 긴 내용으로 답변 작성
        String longContent = "a".repeat(10000);
        var req = new CommunityAnswerCreateReq(questionId, longContent, null);
        var result = communityAnswerService.saveAnswer(TEST_USER_ID, req, List.of());

        assertThat(result).isNotNull();
        assertThat(result.content()).isEqualTo(longContent);
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
                    "VALUES (3200, 1, 1, 'updateAnswer빈내용테스트', '내용', false, false, NOW(), NOW());"
    })
    @Test
    void updateAnswer_공백문자만_예외() {
        Long questionId = createQuestionAndGetId("updateAnswer빈내용테스트", "내용");
        Long answerId = createAnswerAndGetId(questionId, "원본 답변");

        // 공백 문자만으로 수정 시 예외 발생
        var updateReq = new CommunityAnswerUpdateReq(answerId, "   \t\n   ", null, List.of());
        assertThatThrownBy(() -> communityAnswerService.updateAnswer(TEST_USER_ID, answerId, updateReq, List.of()))
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
    void getAllAnswers_생성순서대로정렬_정상() {
        Long questionId = createQuestionAndGetId("순서테스트", "내용");

        // 여러 답변을 순서대로 생성
        communityAnswerService.saveAnswer(TEST_USER_ID, new CommunityAnswerCreateReq(questionId,"첫 번째 답변", null), List.of());
        communityAnswerService.saveAnswer(TEST_USER_ID, new CommunityAnswerCreateReq(questionId,"두 번째 답변", null), List.of());
        communityAnswerService.saveAnswer(TEST_USER_ID, new CommunityAnswerCreateReq(questionId,"세 번째 답변", null), List.of());

        var result = communityAnswerService.getAllAnswers(questionId);

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
        var updateReq = new CommunityAnswerUpdateReq(answerId, "수정된 답변", null, List.of());
        var result = communityAnswerService.updateAnswer(TEST_USER_ID, answerId, updateReq, List.of());

        assertThat(result.isAccepted()).isTrue();

        // 전체 답변 목록에서도 채택 상태 확인
        var allAnswers = communityAnswerService.getAllAnswers(questionId);
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
        var updateReq = new CommunityAnswerUpdateReq(answerId, "수정", null, List.of());
        List<org.springframework.web.multipart.MultipartFile> files = java.util.stream.IntStream.range(0, MAX_FILE_COUNT+1)
                .mapToObj(i -> org.mockito.Mockito.mock(org.springframework.web.multipart.MultipartFile.class))
                .toList();
        files.forEach(f -> org.mockito.Mockito.when(f.isEmpty()).thenReturn(false));
        assertThatThrownBy(() -> communityAnswerService.updateAnswer(TEST_USER_ID, answerId, updateReq, files))
                .isInstanceOf(insty.exception.CustomException.class);
    }
}