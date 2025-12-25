package insty.domain.mention.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import insty.domain.mention.dto.MentionUserSearchReq;
import insty.domain.mention.dto.MentionUserSearchRes;
import insty.domain.mention.implement.MentionNotificationManager;
import insty.domain.mention.implement.MentionParser;
import insty.domain.mention.implement.MentionReader;
import insty.domain.mention.implement.MentionWriter;
import insty.domain.courseqna.implement.CourseAnswerReader;
import insty.domain.user.implement.UserFileReader;
import insty.domain.user.repository.UserRepository;
import insty.global.property.AppProperties;
import insty.model.courseqna.CourseAnswer;
import insty.model.courseqna.CourseQuestion;
import insty.s3.adapter.S3FileManager;
import insty.s3.adapter.S3UrlIssuer;
import insty.cloudfront.adapter.CloudFrontSigner;
import insty.ai.adapter.AiRequester;
import insty.model.mention.Mention;
import insty.model.user.User;
import java.util.List;
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
class MentionServiceTest {

    @Autowired
    private MentionService mentionService;

    @Autowired
    private MentionReader mentionReader;

    @Autowired
    private MentionWriter mentionWriter;

    @Autowired
    private MentionParser mentionParser;

    @MockitoBean
    private MentionNotificationManager mentionNotificationManager;

    @Autowired
    private UserFileReader userFileReader;

    @Autowired
    private CourseAnswerReader courseAnswerReader;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private AppProperties appProperties;
    @MockitoBean
    private S3UrlIssuer s3UrlIssuer;
    @MockitoBean
    private S3FileManager s3FileManager;
    @MockitoBean
    private CloudFrontSigner cloudFrontSigner;
    @MockitoBean
    private AiRequester aiRequester;

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) "
                    + "VALUES (1, 'example@example.com', 'example', 1234, null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) "
                    + "VALUES (2, 'user2@example.com', '홍길동', 1234, null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) "
                    + "VALUES (3, 'user3@example.com', '김철수', 1234, null, 'LEARNER', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, thumbnail_id, is_show, created_at, updated_at, is_deleted) "
                    + "VALUES (1, 1, '테스트 강의', '설명', 20000, 0, 0, '테스트 대상자', null, true, NOW(), NOW(), false);",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, status, created_at, updated_at, is_deleted) "
                    + "VALUES (1, 1, 1, '테스트 질문', '질문 내용', 'WAITING', NOW(), NOW(), false);",
            "INSERT INTO web_service.community_answers (id, question_id, user_id, content, is_accepted, created_at, updated_at, is_deleted) "
                    + "VALUES (1, 1, 2, '테스트 답변 @[홍길동](2)님과 @[김철수](3)님!', false, NOW(), NOW(), false);"
    })
    @Test
    void searchMentionableUsers_정상() {
        // given
        MentionUserSearchReq req = new MentionUserSearchReq("홍길동", 10);
        Long userId = 1L;

        // mock
        when(appProperties.getDomain()).thenReturn("insty.test.com");

        // when
        List<MentionUserSearchRes> result = mentionService.searchMentionableUsers(req, userId);

        // then
        assertThat(result).isNotEmpty();
        assertThat(result.stream().anyMatch(user -> user.nickname().contains("홍길동"))).isTrue();
        assertThat(result.stream().noneMatch(user -> user.id().equals(userId))).isTrue(); // 본인 제외
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) "
                    + "VALUES (1, 'example@example.com', 'example', 1234, null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) "
                    + "VALUES (2, 'user2@example.com', '홍길동', 1234, null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, thumbnail_id, is_show, created_at, updated_at, is_deleted) "
                    + "VALUES (1, 1, '테스트 강의', '설명', 20000, 0, 0, '테스트 대상자', null, true, NOW(), NOW(), false);",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, status, created_at, updated_at, is_deleted) "
                    + "VALUES (1, 1, 1, '테스트 질문', '질문 내용', 'WAITING', NOW(), NOW(), false);",
            "INSERT INTO web_service.community_answers (id, question_id, user_id, content, is_accepted, created_at, updated_at, is_deleted) "
                    + "VALUES (1, 1, 2, '테스트 답변', false, NOW(), NOW(), false);"
    })
    @Test
    void processMentions_정상() {
        // given
        CourseAnswer courseAnswer = courseAnswerReader.getCourseAnswerById(1L);
        User mentionerUser = userRepository.findById(1L).orElseThrow();
        String content = "안녕하세요 @[홍길동](2)님!";

        // when
        mentionService.processMentions(courseAnswer, mentionerUser, content);

        // then
        List<Mention> mentions = mentionReader.getMentionsByAnswerId(1L);
        assertThat(mentions).hasSize(1);
        assertThat(mentions.get(0).getMentionedUser().getId()).isEqualTo(2L);
        assertThat(mentions.get(0).getMentionerUser().getId()).isEqualTo(mentionerUser.getId());
        
        verify(mentionNotificationManager)
                .sendMentionsNotification(
                        argThat(list -> list.size() == 1),
                        any(CourseQuestion.class)
                );
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) "
                    + "VALUES (1, 'example@example.com', 'example', 1234, null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) "
                    + "VALUES (2, 'user2@example.com', '홍길동', 1234, null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, thumbnail_id, is_show, created_at, updated_at, is_deleted) "
                    + "VALUES (1, 1, '테스트 강의', '설명', 20000, 0, 0, '테스트 대상자', null, true, NOW(), NOW(), false);",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, status, created_at, updated_at, is_deleted) "
                    + "VALUES (1, 1, 1, '테스트 질문', '질문 내용', 'WAITING', NOW(), NOW(), false);",
            "INSERT INTO web_service.community_answers (id, question_id, user_id, content, is_accepted, created_at, updated_at, is_deleted) "
                    + "VALUES (1, 1, 2, '테스트 답변', false, NOW(), NOW(), false);"
    })
    @Test
    void processMentions_멘션없음_정상() {
        // given
        CourseAnswer courseAnswer = courseAnswerReader.getCourseAnswerById(1L);
        User mentionerUser = userRepository.findById(1L).orElseThrow();
        String content = "안녕하세요! 멘션 없습니다.";

        // when
        mentionService.processMentions(courseAnswer, mentionerUser, content);

        // then
        List<Mention> mentions = mentionReader.getMentionsByAnswerId(1L);
        assertThat(mentions).isEmpty();
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) "
                    + "VALUES (1, 'example@example.com', 'example', 1234, null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) "
                    + "VALUES (2, 'user2@example.com', '홍길동', 1234, null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) "
                    + "VALUES (3, 'user3@example.com', '김철수', 1234, null, 'LEARNER', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, thumbnail_id, is_show, created_at, updated_at, is_deleted) "
                    + "VALUES (1, 1, '테스트 강의', '설명', 20000, 0, 0, '테스트 대상자', null, true, NOW(), NOW(), false);",
            "INSERT INTO web_service.community_questions (id, course_id, user_id, title, content, status, created_at, updated_at, is_deleted) "
                    + "VALUES (1, 1, 1, '테스트 질문', '질문 내용', 'WAITING', NOW(), NOW(), false);",
            "INSERT INTO web_service.community_answers (id, question_id, user_id, content, is_accepted, created_at, updated_at, is_deleted) "
                    + "VALUES (1, 1, 2, '테스트 답변', false, NOW(), NOW(), false);"
    })
    @Test
    void processMentions_다중멘션_정상() {
        // given
        CourseAnswer courseAnswer = courseAnswerReader.getCourseAnswerById(1L);
        User mentionerUser = userRepository.findById(1L).orElseThrow();
        String content = "안녕하세요 @[홍길동](2)님과 @[김철수](3)님!";

        // when
        mentionService.processMentions(courseAnswer, mentionerUser, content);

        // then
        List<Mention> mentions = mentionReader.getMentionsByAnswerId(1L);
        assertThat(mentions).hasSize(2);
        assertThat(mentions.stream().map(m -> m.getMentionedUser().getId()).toList())
                .containsExactlyInAnyOrder(2L, 3L);
        assertThat(mentions.stream().allMatch(m -> m.getMentionerUser().getId().equals(mentionerUser.getId()))).isTrue();
        
        verify(mentionNotificationManager)
                .sendMentionsNotification(
                        argThat(list -> list.size() == 2),
                        any(CourseQuestion.class)
                );
    }
}
