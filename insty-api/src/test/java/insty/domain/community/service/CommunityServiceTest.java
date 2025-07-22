package insty.domain.community.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import insty.domain.community.dto.CommunityAnswerCreateReq;
import insty.domain.community.dto.CommunityAnswerRes;
import insty.domain.community.dto.CommunityQuestionCreateReq;
import insty.domain.community.dto.CommunityQuestionRes;
import insty.domain.community.dto.CommunityQuestionUpdateReq;
import insty.domain.community.implement.CommunityQuestionReader;
import insty.domain.community.implement.CommunityQuestionWriter;
import insty.domain.community.implement.CommunityAnswerReader;
import insty.domain.user.implement.UserReader;
import insty.model.community.CommunityQuestion;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import insty.exception.CustomException;
import insty.domain.community.dto.CommunityQuestionSearchReq;
import org.junit.jupiter.api.BeforeEach;
import insty.s3.adapter.S3FileManager;
import insty.s3.adapter.S3UrlIssuer;
import insty.cloudfront.adapter.CloudFrontSigner;
import insty.global.property.AppProperties;
import insty.domain.community.repository.CommunityQuestionRepository;
import java.util.Optional;
import jakarta.persistence.EntityManager;
import insty.domain.community.repository.CommunityAnswerRepository;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Tag("integration")
class CommunityServiceTest {

    @Autowired
    private CommunityService communityService;

    @Autowired
    private CommunityQuestionReader communityQuestionReader;
    @Autowired
    private CommunityQuestionWriter communityQuestionWriter;
    @Autowired
    private UserReader userReader;
    @Autowired
    private CommunityAnswerReader communityAnswerReader;
    @Autowired
    private CommunityQuestionRepository communityQuestionRepository;
    @Autowired
    private CommunityAnswerRepository communityAnswerRepository;
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

    private Long createQuestionAndGetId(String title, String content) {
        var req = new CommunityQuestionCreateReq(TEST_COURSE_ID, TEST_USER_ID, title, content, List.of());
        communityService.saveQuestion(req, List.of());
        return communityQuestionReader.getAllCommunityQuestionsByCourseId(TEST_COURSE_ID).stream()
                .filter(q -> q.getTitle().equals(title))
                .findFirst()
                .map(insty.model.community.CommunityQuestion::getId)
                .orElseThrow();
    }

    private Long createAnswerAndGetId(Long questionId, String content) {
        communityService.saveAnswer(new CommunityAnswerCreateReq(questionId, TEST_USER_ID, content, null), List.of());
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
                    "VALUES (10, 1, 1, '상세조회 제목', '상세조회 내용', false, false, NOW(), NOW());"
    })
    @Test
    void createQuestion_정상() {
        // given
        Long userId = 1L;
        Long courseId = 1L;
        String title = "질문 제목";
        String content = "질문 내용";
        CommunityQuestionCreateReq req = new CommunityQuestionCreateReq(courseId, userId, title, content, List.of());

        // when
        CommunityQuestionRes res = communityService.saveQuestion(req, List.of());

        // then
        assertThat(res).isNotNull();
        assertThat(res.title()).isEqualTo(title);
        assertThat(res.content()).isEqualTo(content);
        assertThat(res.userId()).isEqualTo(userId);

        // 질문 id를 title로 찾아서 조회
        Long questionId = communityQuestionReader.getAllCommunityQuestionsByCourseId(courseId).stream()
                .filter(q -> q.getTitle().equals(title))
                .findFirst()
                .map(insty.model.community.CommunityQuestion::getId)
                .orElseThrow();
        CommunityQuestion question = communityQuestionReader.getCommunityQuestionDetailsById(questionId);
        assertThat(question).isNotNull();
        assertThat(question.getTitle()).isEqualTo(title);
        assertThat(question.getContent()).isEqualTo(content);
    }

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
        CommunityQuestionRes res = communityService.getQuestionDetails(questionId);

        // then
        assertThat(res).isNotNull();
        assertThat(res.title()).isEqualTo("상세조회 제목");
        assertThat(res.content()).isEqualTo("상세조회 내용");
        assertThat(res.userId()).isEqualTo(1L);
        assertThat(res.courseId()).isEqualTo(1L);
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (20, 1, 1, '수정전 제목', '수정전 내용', false, false, NOW(), NOW());"
    })
    @Test
    void updateQuestion_정상() {
        // given
        Long questionId = 20L;
        String newTitle = "수정된 제목";
        String newContent = "수정된 내용";
        CommunityQuestionUpdateReq req = new CommunityQuestionUpdateReq(questionId, newTitle, newContent, List.of(), List.of());

        // when
        CommunityQuestionRes res = communityService.updateQuestion(questionId, req, List.of());

        // then
        assertThat(res).isNotNull();
        assertThat(res.title()).isEqualTo(newTitle);
        assertThat(res.content()).isEqualTo(newContent);
        assertThat(res.userId()).isEqualTo(1L);
        assertThat(res.courseId()).isEqualTo(1L);
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (30, 1, 1, '삭제할 제목', '삭제할 내용', false, false, NOW(), NOW());"
    })
    @Test
    void deleteQuestion_정상() {
        // given
        Long questionId = 30L;

        // when
        communityService.deleteQuestion(questionId);
        entityManager.flush();
        entityManager.clear();

        // then
        Optional<insty.model.community.CommunityQuestion> questionOpt = communityQuestionRepository.findById(questionId);
        assertThat(questionOpt).isPresent();
        assertThat(questionOpt.get().isDeleted()).isTrue();
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

        var res = communityService.getAllAnswers(questionId).stream()
                .filter(a -> a.content().equals(content))
                .findFirst()
                .orElse(null);

        assertThat(res).isNotNull();
        assertThat(res.userId()).isEqualTo(TEST_USER_ID);
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
        var updateReq = new insty.domain.community.dto.CommunityAnswerUpdateReq(answerId, newContent, null, List.of());
        var updatedRes = communityService.updateAnswer(answerId, updateReq, List.of());

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

        communityService.deleteAnswer(answerId);
        entityManager.flush();
        entityManager.clear();

        // then: 논리 삭제 후 DB에서 직접 조회하여 isDeleted=true 검증
        Optional<insty.model.community.CommunityAnswer> answerOpt = communityAnswerRepository.findById(answerId);
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

        var result = communityService.acceptAnswer(questionId, answerId);

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
                    "VALUES (80, 1, 1, '삭제테스트 질문', '삭제테스트 질문 내용', false, false, NOW(), NOW());"
    })
    @Test
    void deleteQuestion_이미삭제된질문_예외() {
        // given
        Long questionId = 80L;
        // 1차 삭제
        communityService.deleteQuestion(questionId);
        // 2차 삭제 시도
        assertThatThrownBy(() -> communityService.deleteQuestion(questionId))
                .isInstanceOf(CustomException.class);
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
        communityService.deleteAnswer(answerId);
        entityManager.flush();
        entityManager.clear();
        // 삭제된 답변 수정 시도 시 예외 발생 검증
        var updateReq = new insty.domain.community.dto.CommunityAnswerUpdateReq(answerId, "수정", null, List.of());
        assertThatThrownBy(() -> communityService.updateAnswer(answerId, updateReq, List.of()))
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
        var req = new CommunityAnswerCreateReq(questionId, TEST_USER_ID, null, null);
        assertThatThrownBy(() -> communityService.saveAnswer(req, List.of()))
                .isInstanceOf(CustomException.class);
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (101, 1, 1, '파일없는질문', '내용', false, false, NOW(), NOW());"
    })
    @Test
    void saveQuestion_첨부파일비디오없음_정상() {
        String title = "파일없는질문", content = "내용";
        var req = new CommunityQuestionCreateReq(TEST_COURSE_ID, TEST_USER_ID, title, content, List.of());
        var res = communityService.saveQuestion(req, List.of());
        assertThat(res).isNotNull();
        assertThat(res.attachments()).isEmpty();
        assertThat(res.videoInfos()).isNull(); // 또는 .isEmpty() 등 실제 반환값에 따라
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
        var req = new CommunityAnswerCreateReq(questionId, TEST_USER_ID, content, null);
        var res = communityService.saveAnswer(req, List.of());
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
                    "VALUES (400, 1, 1, '검색질문1', '내용1', false, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (401, 1, 1, '검색질문2', '내용2', false, false, NOW(), NOW());"
    })
    @Test
    void searchQuestions_정상() {
        var req = new CommunityQuestionSearchReq(1, 10, null, null, "검색질문", null);
        var res = communityService.searchQuestions(req);
        assertThat(res).isNotNull();
        assertThat(res.items()).hasSizeGreaterThanOrEqualTo(2);
        assertThat(res.items().get(0).title()).contains("검색질문");
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) " +
                    "VALUES (1, 'user@example.com', 'user', 'pw', null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, is_show, is_deleted, created_at, updated_at) " +
                    "VALUES (1, 1, '테스트 강의', '설명', 10000, 0, 0, '초보자', true, false, NOW(), NOW());",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, is_answered, is_deleted, created_at, updated_at) " +
                    "VALUES (500, 1, 1, '목록질문', '내용', false, false, NOW(), NOW());"
    })
    @Test
    void getQuestionsByCourseId_정상() {
        var res = communityService.getQuestionsByCourseId(1L);
        assertThat(res).isNotNull();
        assertThat(res).isNotEmpty();
        assertThat(res.get(0).courseId()).isEqualTo(1L);
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
        communityService.saveAnswer(new CommunityAnswerCreateReq(questionId, userId, "답변1", null), List.of());
        communityService.saveAnswer(new CommunityAnswerCreateReq(questionId, userId, "답변2", null), List.of());

        var res = communityService.getAllAnswers(questionId);
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
                    "VALUES (120, 1, 1, '수정불가질문', '수정불가질문 내용', false, false, NOW(), NOW());"
    })
    @Test
    void updateQuestion_이미삭제된질문_예외() {
        Long questionId = 120L;
        // 논리 삭제
        communityService.deleteQuestion(questionId);
        entityManager.flush();
        entityManager.clear();
        // 삭제된 질문 수정 시도 시 예외 발생 검증
        var updateReq = new insty.domain.community.dto.CommunityQuestionUpdateReq(questionId, "수정된 제목", "수정된 내용", List.of(), List.of());
        assertThatThrownBy(() -> communityService.updateQuestion(questionId, updateReq, List.of()))
                .isInstanceOf(CustomException.class);
    }
}