package insty.domain.community.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import insty.ai.adapter.AiRequester;
import insty.cloudfront.adapter.CloudFrontSigner;
import insty.domain.common.FileInfo;
import insty.domain.common.SearchRes;
import insty.domain.community.dto.CommunityQuestionCreateReq;
import insty.domain.community.dto.CommunityQuestionDetailsRes;
import insty.domain.community.dto.CommunityQuestionMyRes;
import insty.domain.community.dto.CommunityQuestionRes;
import insty.domain.community.dto.CommunityQuestionSearchReq;
import insty.domain.community.dto.CommunityQuestionUpdateReq;
import insty.domain.community.implement.CommunityAnswerFileReader;
import insty.domain.community.implement.CommunityAnswerFileWriter;
import insty.domain.community.implement.CommunityAnswerMapper;
import insty.domain.community.implement.CommunityAnswerReader;
import insty.domain.community.implement.CommunityAnswerVideoManager;
import insty.domain.community.implement.CommunityAnswerWriter;
import insty.domain.community.implement.CommunityQuestionFileReader;
import insty.domain.community.implement.CommunityQuestionFileWriter;
import insty.domain.community.implement.CommunityQuestionReader;
import insty.domain.community.implement.CommunityQuestionStatusManager;
import insty.domain.community.implement.CommunityQuestionVideoManager;
import insty.domain.community.implement.CommunityQuestionWriter;
import insty.domain.community.implement.CommunityValidator;
import insty.domain.community.repository.CommunityQuestionViewRepository;
import insty.domain.course.implement.CourseReader;
import insty.domain.user.implement.UserReader;
import insty.domain.video.repository.VideoEncodingRepository;
import insty.global.property.AppProperties;
import insty.model.community.CommunityBoardType;
import insty.model.community.CommunityQuestionView;
import insty.model.community.QuestionStatus;
import static org.mockito.Mockito.mock;

import insty.model.user.UserType;
import insty.s3.adapter.S3FileManager;
import insty.s3.adapter.S3UrlIssuer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    private CommunityQuestionFileReader communityQuestionFileReader;
    @Autowired
    private CommunityQuestionFileWriter communityQuestionFileWriter;
    @Autowired
    private CommunityQuestionVideoManager communityQuestionVideoManager;
    @Autowired
    private CommunityValidator communityValidator;
    @Autowired
    private CommunityQuestionStatusManager communityQuestionStatusManager;
    @Autowired
    private CommunityAnswerService communityAnswerService;
    @Autowired
    private CommunityAnswerWriter communityAnswerWriter;
    @Autowired
    private CommunityAnswerFileReader communityAnswerFileReader;
    @Autowired
    private CommunityAnswerFileWriter communityAnswerFileWriter;
    @Autowired
    private CommunityAnswerVideoManager communityAnswerVideoManager;
    @Autowired
    private CommunityAnswerMapper communityAnswerMapper;
    @Autowired
    private UserReader userReader;
    @Autowired
    private CourseReader courseReader;
    @Autowired
    private CommunityAnswerReader communityAnswerReader;
    @Autowired
    private CommunityQuestionViewRepository communityQuestionViewRepository;

    @MockitoBean
    private AppProperties appProperties;
    @MockitoBean
    private S3FileManager s3FileManager;
    @MockitoBean
    private S3UrlIssuer s3UrlIssuer;
    @MockitoBean
    private CloudFrontSigner cloudFrontSigner;
    @MockitoBean
    private AiRequester aiRequester;
    @MockitoBean
    private VideoEncodingRepository videoEncodingRepository;

    /**
     * 질문 생성: 첨부파일/비디오 연동을 검증한다.
     */
    @Test
    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) "
                    + "VALUES (1, 'question_author@example.com', '질문작성자', 1234, null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) "
                    + "VALUES (2, 'course_creator@example.com', '강의제작자', 1234, null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, thumbnail_id, is_show, created_at, updated_at, is_deleted) "
                    + "VALUES (1, 2, '테스트 강의', '테스트 강의 설명', 20000, 0, 0, '테스트 대상자', null, true, NOW(), NOW(), false);",
            "INSERT INTO web_service.video_questions (id, video_uuid, community_question_id, user_id, s3key, extension, original_file_name, duration, encoding_status, encoding_at, created_at, updated_at, is_deleted) "
                    + "VALUES (1, '00000000-0000-0000-0000-000000000001', null, 1, 'vod/QUESTION/mp4/00000000-0000-0000-0000-000000000001/question_video.mp4', 'mp4', 'question_video.mp4', 12, 'PROCESSING', NOW(), NOW(), NOW(), false)"})
    void saveQuestion_정상() {
        Long userId = 1L;
        Long courseId = 1L;
        UUID videoUuid = UUID.fromString("00000000-0000-0000-0000-000000000001");
        CommunityQuestionCreateReq req = new CommunityQuestionCreateReq(courseId, "테스트 질문 제목", "테스트 질문 내용", CommunityBoardType.QNA, videoUuid);

        List<MultipartFile> attachments = List.of(
                new MockMultipartFile("attachment", "question_img1.jpg", "image/jpeg", "q-content-1".getBytes()));

        when(appProperties.getDomain()).thenReturn("insty.test.com");
        when(s3FileManager.upload(any(), anyString(), anyString())).thenReturn("question_img1.jpg");

        CommunityQuestionDetailsRes res = communityQuestionService.saveQuestion(userId, req, attachments);

        assertThat(res).isNotNull();
        assertThat(res.user()).isNotNull();
        assertThat(res.user().id()).isEqualTo(userId);
        assertThat(res.user().nickname()).isEqualTo("질문작성자");
        assertThat(res.user().userType()).isEqualTo(UserType.CREATOR);
        assertThat(res.courseId()).isEqualTo(courseId);
        assertThat(res.title()).isEqualTo("테스트 질문 제목");
        assertThat(res.content()).isEqualTo("테스트 질문 내용");
        assertThat(res.attachments()).hasSize(1);
        assertThat(res.attachments().get(0).name()).isEqualTo("question_img1.jpg");
        assertThat(res.attachments().get(0).contentType()).isEqualTo("image/jpeg");
        assertThat(res.videoInfo()).isNotNull();
        assertThat(res.videoInfo().videoUuid()).isEqualTo(videoUuid);
        assertThat(res.videoInfo().originFileName()).isEqualTo("question_video.mp4");
    }

    /**
     * 질문 수정: 첨부파일 교체와 비디오 교체를 검증한다.
     */
    @Test
    @Sql(statements = {
            // 사용자 2명 (질문자, 강의자)
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) "
                    + "VALUES (1, 'question_author@example.com', '질문작성자', 1234, null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) "
                    + "VALUES (2, 'course_creator@example.com', '강의제작자', 1234, null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",

            // 강의 1개
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, thumbnail_id, is_show, created_at, updated_at, is_deleted) "
                    + "VALUES (1, 2, '테스트 강의', '테스트 강의 설명', 20000, 0, 0, '테스트 대상자', null, true, NOW(), NOW(), false);",

            // 질문 1개 (초기 상태 ANSWERED)
            "INSERT INTO web_service.community_questions (id, user_id, course_id, title, content, status, created_at, updated_at, is_deleted) "
                    + "VALUES (1, 1, 1, '기존 질문 제목', '기존 질문 내용', 'ANSWERED', NOW(), NOW(), false);",

            // 질문 첨부 파일 2개
            "INSERT INTO web_service.files (id, container_id, container_type, content_type, name, original_name, size, created_at, updated_at) "
                    + "VALUES (100, 1, 'QUESTION_IMAGE', 'image/jpeg', 'old_q_attachment1.jpg', 'old_q_attachment1.jpg', 1024, NOW(), NOW());",
            "INSERT INTO web_service.files (id, container_id, container_type, content_type, name, original_name, size, created_at, updated_at) "
                    + "VALUES (101, 1, 'QUESTION_IMAGE', 'image/png', 'old_q_attachment2.png', 'old_q_attachment2.png', 2048, NOW(), NOW());",
            "INSERT INTO web_service.community_question_files (question_id, file_id, created_at, updated_at) VALUES (1, 100, NOW(), NOW());",
            "INSERT INTO web_service.community_question_files (question_id, file_id, created_at, updated_at) VALUES (1, 101, NOW(), NOW());",

            // 질문 비디오 - 기존 연결된 비디오와, 새로 연결할 비디오(아직 미연결)
            "INSERT INTO web_service.video_questions (id, video_uuid, community_question_id, user_id, s3key, extension, original_file_name, duration, encoding_status, encoding_at, created_at, updated_at, is_deleted) "
                    + "VALUES (10, '00000000-0000-0000-0000-000000000010', 1, 1, 'vod/QUESTION/mp4/00000000-0000-0000-0000-000000000010/old_question_video.mp4', 'mp4', 'old_question_video.mp4', 15, 'COMPLETED', NOW(), NOW(), NOW(), false);",
            "INSERT INTO web_service.video_questions (id, video_uuid, community_question_id, user_id, s3key, extension, original_file_name, duration, encoding_status, encoding_at, created_at, updated_at, is_deleted) "
                    + "VALUES (11, '00000000-0000-0000-0000-000000000011', null, 1, 'vod/QUESTION/mp4/00000000-0000-0000-0000-000000000011/new_question_video.mp4', 'mp4', 'new_question_video.mp4', 20, 'COMPLETED', NOW(), NOW(), NOW(), false);",

            // 답변 2개 (하나는 비디오 포함, 하나는 파일 포함)
            "INSERT INTO web_service.community_answers (id, user_id, question_id, content, is_accepted, created_at, updated_at, is_deleted) "
                    + "VALUES (1, 1, 1, '기존 답변 내용 A', false, DATEADD('MINUTE', -20, NOW()), NOW(), false);",
            "INSERT INTO web_service.community_answers (id, user_id, question_id, content, is_accepted, created_at, updated_at, is_deleted) "
                    + "VALUES (2, 2, 1, '기존 답변 내용 B', false, DATEADD('MINUTE', -10, NOW()), NOW(), false);",
            "INSERT INTO web_service.files (id, container_id, container_type, content_type, name, original_name, size, created_at, updated_at) "
                    + "VALUES (200, 2, 'ANSWER_IMAGE', 'image/jpeg', 'answer_attachment1.jpg', 'answer_attachment1.jpg', 512, NOW(), NOW());",
            "INSERT INTO web_service.community_answers_files (answer_id, file_id, created_at, updated_at) VALUES (2, 200, NOW(), NOW());",
            "INSERT INTO web_service.video_answers (id, video_uuid, community_answer_id, user_id, s3key, extension, original_file_name, duration, encoding_status, encoding_at, created_at, updated_at, is_deleted) "
                    + "VALUES (20, '00000000-0000-0000-0000-000000000020', 1, 1, 'vod/ANSWER/mp4/00000000-0000-0000-0000-000000000020/answer_video.mp4', 'mp4', 'answer_video.mp4', 10, 'COMPLETED', NOW(), NOW(), NOW(), false);"})
    void updateQuestion_정상() {
        Long userId = 1L;
        Long questionId = 1L;

        var before = communityQuestionReader.getCommunityQuestionWithFilesById(questionId);
        List<FileInfo> existingFiles = communityQuestionFileReader.getQuestionFileInfos(before);
        assertThat(existingFiles).hasSize(2);
        List<Long> deleteFileIds = existingFiles.stream().map(FileInfo::id).toList();

        CommunityQuestionUpdateReq req = new CommunityQuestionUpdateReq("수정된 질문 제목", "수정된 질문 내용",
                UUID.fromString("00000000-0000-0000-0000-000000000011"), deleteFileIds);

        List<MultipartFile> newAttachments = List.of(
                new MockMultipartFile("attachment1", "new_q_attachment1.jpg", "image/jpeg", "new_q_1".getBytes()),
                new MockMultipartFile("attachment2", "new_q_attachment2.png", "image/png", "new_q_2".getBytes()));

        when(appProperties.getDomain()).thenReturn("insty.test.com");
        when(s3FileManager.upload(any(), anyString(), anyString())).thenReturn("new_q_attachment1.jpg")
                .thenReturn("new_q_attachment2.png");

        when(videoEncodingRepository.findByVideoUuid(any())).thenReturn(Optional.of(mock(insty.model.video.VideoEncoding.class)));

        CommunityQuestionDetailsRes res = communityQuestionService.updateQuestion(userId, questionId, req,
                newAttachments);

        assertThat(res).isNotNull();
        assertThat(res.title()).isEqualTo("수정된 질문 제목");
        assertThat(res.content()).isEqualTo("수정된 질문 내용");

        assertThat(res.attachments()).hasSize(2);
        assertThat(res.attachments().get(0).name()).isEqualTo("new_q_attachment1.jpg");
        assertThat(res.attachments().get(0).contentType()).isEqualTo("image/jpeg");
        assertThat(res.attachments().get(1).name()).isEqualTo("new_q_attachment2.png");
        assertThat(res.attachments().get(1).contentType()).isEqualTo("image/png");

        assertThat(res.videoInfo()).isNotNull();
        assertThat(res.videoInfo().videoUuid()).isEqualTo(UUID.fromString("00000000-0000-0000-0000-000000000011"));
        assertThat(res.videoInfo().originFileName()).isEqualTo("new_question_video.mp4");

    }

    /**
     * 질문 검색: 키워드/페이지네이션/상태 필터를 검증한다.
     */
    @Test
    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) "
                    + "VALUES (1, 'user1@example.com', '사용자1', 1234, null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, thumbnail_id, is_show, created_at, updated_at, is_deleted) "
                    + "VALUES (1, 1, '코스1', '설명1', 10000, 0, 0, '대상', null, true, NOW(), NOW(), false);",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, thumbnail_id, is_show, created_at, updated_at, is_deleted) "
                    + "VALUES (2, 1, '코스2', '설명2', 20000, 0, 0, '대상', null, true, NOW(), NOW(), false);",

            "INSERT INTO web_service.community_questions (id, user_id, course_id, title, content, status, created_at, updated_at, is_deleted) "
                    + "VALUES (1, 1, 1, '[키워드] 제목 포함', '내용 일반', 'ANSWERED', DATEADD('MINUTE', -20, NOW()), NOW(), false);",
            "INSERT INTO web_service.community_questions (id, user_id, course_id, title, content, status, created_at, updated_at, is_deleted) "
                    + "VALUES (2, 1, 1, '일반 제목', '내용에 [키워드] 포함', 'ANSWERED', DATEADD('MINUTE', -10, NOW()), NOW(), false);",
            "INSERT INTO web_service.community_questions (id, user_id, course_id, title, content, status, created_at, updated_at, is_deleted) "
                    + "VALUES (3, 1, 1, '일반3', '일반 내용3', 'ANSWERED', DATEADD('MINUTE', -30, NOW()), NOW(), false);",
            "INSERT INTO web_service.community_questions (id, user_id, course_id, title, content, status, created_at, updated_at, is_deleted) "
                    + "VALUES (4, 1, 1, '[키워드] 있으나 대기', '내용', 'WAITING', DATEADD('MINUTE', -5, NOW()), NOW(), false);",
            "INSERT INTO web_service.community_questions (id, user_id, course_id, title, content, status, created_at, updated_at, is_deleted) "
                    + "VALUES (5, 1, 1, '[키워드] ACCEPTED', '내용', 'ACCEPTED', DATEADD('MINUTE', -2, NOW()), NOW(), false);",
            "INSERT INTO web_service.community_questions (id, user_id, course_id, title, content, status, created_at, updated_at, is_deleted) "
                    + "VALUES (6, 1, 1, '[키워드] A6', '내용', 'ANSWERED', DATEADD('MINUTE', -3, NOW()), NOW(), false);",
            "INSERT INTO web_service.community_questions (id, user_id, course_id, title, content, status, created_at, updated_at, is_deleted) "
                    + "VALUES (7, 1, 1, '[키워드] A7', '내용', 'ANSWERED', DATEADD('MINUTE', -4, NOW()), NOW(), false);",
            "INSERT INTO web_service.community_questions (id, user_id, course_id, title, content, status, created_at, updated_at, is_deleted) "
                    + "VALUES (8, 1, 1, '[키워드] A8', '내용', 'ANSWERED', DATEADD('MINUTE', -8, NOW()), NOW(), false);",
            "INSERT INTO web_service.community_questions (id, user_id, course_id, title, content, status, created_at, updated_at, is_deleted) "
                    + "VALUES (9, 1, 1, '[키워드] A9', '내용', 'ANSWERED', DATEADD('MINUTE', -6, NOW()), NOW(), false);",
            "INSERT INTO web_service.community_questions (id, user_id, course_id, title, content, status, created_at, updated_at, is_deleted) "
                    + "VALUES (10, 1, 1, '[키워드] WAIT2', '내용', 'WAITING', DATEADD('MINUTE', -7, NOW()), NOW(), false);",
            "INSERT INTO web_service.community_questions (id, user_id, course_id, title, content, status, created_at, updated_at, is_deleted) "
                    + "VALUES (11, 1, 2, '[키워드] 다른코스 WAIT', '내용', 'WAITING', DATEADD('MINUTE', -1, NOW()), NOW(), false);"})
    void searchQuestions_정상() {
        CommunityQuestionSearchReq req = new CommunityQuestionSearchReq(1, 5, null, null, "키워드", java.util.List.of(QuestionStatus.ANSWERED, QuestionStatus.ACCEPTED), null);

        SearchRes<CommunityQuestionRes> res = communityQuestionService.searchQuestions(req);

        assertThat(res).isNotNull();
        assertThat(res.items()).hasSize(5);
        assertThat(
                res.items().stream().allMatch(q -> q.title().contains("키워드") || q.content().contains("키워드"))).isTrue();
        assertThat(res.items().stream().allMatch(q -> q.status() != QuestionStatus.WAITING)).isTrue();
        assertThat(res.items().get(0).createdAt()).isAfter(res.items().get(1).createdAt());
        assertThat(res.pagination().currentPage()).isEqualTo(1);
        assertThat(res.pagination().perPage()).isEqualTo(5);
        assertThat(res.pagination().totalItems()).isEqualTo(7);

        CommunityQuestionSearchReq page2Req = new CommunityQuestionSearchReq(2, 5, null, null, "키워드", java.util.List.of(QuestionStatus.ANSWERED, QuestionStatus.ACCEPTED), null);

        SearchRes<CommunityQuestionRes> resPage2 = communityQuestionService.searchQuestions(page2Req);
        assertThat(resPage2.items()).hasSize(2);
        assertThat(resPage2.items().get(0).createdAt()).isAfter(resPage2.items().get(1).createdAt());

        CommunityQuestionSearchReq waitingReq = new CommunityQuestionSearchReq(1, 10, null, null, "키워드", java.util.List.of(QuestionStatus.WAITING), null);

        SearchRes<CommunityQuestionRes> waitingRes = communityQuestionService.searchQuestions(waitingReq);
        assertThat(waitingRes.items()).extracting(CommunityQuestionRes::status).containsOnly(QuestionStatus.WAITING);
    }

    /**
     * 사용자별 질문 검색 테스트
     */
    @Test
    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) "
                    + "VALUES (1, 'user1@example.com', '사용자1', 1234, null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) "
                    + "VALUES (2, 'user2@example.com', '사용자2', 1234, null, 'LEARNER', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, thumbnail_id, is_show, created_at, updated_at, is_deleted) "
                    + "VALUES (1, 1, '코스1', '설명1', 10000, 0, 0, '대상', null, true, NOW(), NOW(), false);",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, thumbnail_id, is_show, created_at, updated_at, is_deleted) "
                    + "VALUES (2, 2, '코스2', '설명2', 20000, 0, 0, '대상', null, true, NOW(), NOW(), false);",

            // user1 질문 3개
            "INSERT INTO web_service.community_questions (id, user_id, course_id, title, content, status, created_at, updated_at, is_deleted) "
                    + "VALUES (1, 1, 1, 'U1-Q1', '내용1', 'ANSWERED', DATEADD('MINUTE', -30, NOW()), NOW(), false);",
            "INSERT INTO web_service.community_questions (id, user_id, course_id, title, content, status, created_at, updated_at, is_deleted) "
                    + "VALUES (2, 1, 1, 'U1-Q2', '내용2', 'WAITING', DATEADD('MINUTE', -20, NOW()), NOW(), false);",
            "INSERT INTO web_service.community_questions (id, user_id, course_id, title, content, status, created_at, updated_at, is_deleted) "
                    + "VALUES (3, 1, 2, 'U1-Q3', '내용3', 'ACCEPTED', DATEADD('MINUTE', -10, NOW()), NOW(), true);",

            // user2 질문 2개
            "INSERT INTO web_service.community_questions (id, user_id, course_id, title, content, status, created_at, updated_at, is_deleted) "
                    + "VALUES (4, 2, 2, 'U2-Q1', '내용4', 'ANSWERED', DATEADD('MINUTE', -5, NOW()), NOW(), false);",
            "INSERT INTO web_service.community_questions (id, user_id, course_id, title, content, status, created_at, updated_at, is_deleted) "
                    + "VALUES (5, 2, 1, 'U2-Q2', '내용5', 'WAITING', DATEADD('MINUTE', -15, NOW()), NOW(), false);",

            // 답변 데이터
            "INSERT INTO web_service.community_answers (id, user_id, question_id, content, is_accepted, created_at, updated_at, is_deleted) "
                    + "VALUES (1, 2, 1, '질문1 답변1', false, DATEADD('MINUTE', -25, NOW()), NOW(), false);",
            "INSERT INTO web_service.community_answers (id, user_id, question_id, content, is_accepted, created_at, updated_at, is_deleted) "
                    + "VALUES (2, 1, 1, '질문1 답변2', true, DATEADD('MINUTE', -20, NOW()), NOW(), false);",
            "INSERT INTO web_service.community_answers (id, user_id, question_id, content, is_accepted, created_at, updated_at, is_deleted) "
                    + "VALUES (3, 2, 2, '질문2 답변1', false, DATEADD('MINUTE', -15, NOW()), NOW(), false);",
            "INSERT INTO web_service.community_answers (id, user_id, question_id, content, is_accepted, created_at, updated_at, is_deleted) "
                    + "VALUES (4, 1, 4, '질문4 답변1', false, DATEADD('MINUTE', -10, NOW()), NOW(), false);",
            "INSERT INTO web_service.community_answers (id, user_id, question_id, content, is_accepted, created_at, updated_at, is_deleted) "
                    + "VALUES (5, 2, 4, '질문4 답변2', false, DATEADD('MINUTE', -8, NOW()), NOW(), false);",
            "INSERT INTO web_service.community_answers (id, user_id, question_id, content, is_accepted, created_at, updated_at, is_deleted) "
                    + "VALUES (6, 1, 4, '질문4 답변3', false, DATEADD('MINUTE', -5, NOW()), NOW(), false);",

            // 질문1 조회 기록
            "INSERT INTO web_service.community_question_views (question_id, user_id, last_viewed_at) "
                    + "VALUES (1, 1, DATEADD('MINUTE', -22, NOW()));",

            // 질문1 새로운 답변
            "INSERT INTO web_service.community_answers (id, user_id, question_id, content, is_accepted, created_at, updated_at, is_deleted) "
                    + "VALUES (7, 2, 1, '질문1 새로운 답변', false, DATEADD('MINUTE', -18, NOW()), NOW(), false);",

            // 질문6 데이터
            "INSERT INTO web_service.community_questions (id, user_id, course_id, title, content, status, created_at, updated_at, is_deleted) "
                    + "VALUES (6, 1, 1, 'U1-Q4-조회완료', '내용4', 'ANSWERED', DATEADD('MINUTE', -40, NOW()), NOW(), false);",
            "INSERT INTO web_service.community_answers (id, user_id, question_id, content, is_accepted, created_at, updated_at, is_deleted) "
                    + "VALUES (8, 2, 6, '질문6 답변1', false, DATEADD('MINUTE', -35, NOW()), NOW(), false);",
            "INSERT INTO web_service.community_answers (id, user_id, question_id, content, is_accepted, created_at, updated_at, is_deleted) "
                    + "VALUES (9, 2, 6, '질문6 답변2', false, DATEADD('MINUTE', -32, NOW()), NOW(), false);",
            
            // 질문6 조회 기록
            "INSERT INTO web_service.community_question_views (question_id, user_id, last_viewed_at) "
                    + "VALUES (6, 1, DATEADD('MINUTE', -28, NOW()));",
            
            // 질문7 데이터
            "INSERT INTO web_service.community_questions (id, user_id, course_id, title, content, status, created_at, updated_at, is_deleted) "
                    + "VALUES (7, 1, 1, 'U1-Q5-자기답변', '내용5', 'ANSWERED', DATEADD('MINUTE', -50, NOW()), NOW(), false);",
            
            // 질문7에 대한 조회 기록
            "INSERT INTO web_service.community_question_views (question_id, user_id, last_viewed_at) "
                    + "VALUES (7, 1, DATEADD('MINUTE', -45, NOW()));",
            
            // 질문7 자기 답변
            "INSERT INTO web_service.community_answers (id, user_id, question_id, content, is_accepted, created_at, updated_at, is_deleted) "
                    + "VALUES (10, 1, 7, '질문7 자기답변', false, DATEADD('MINUTE', -38, NOW()), NOW(), false);"})
    void searchQuestionsByUserId_정상() {
        CommunityQuestionSearchReq req = new CommunityQuestionSearchReq(1, 10, null, null, null, null, null);

        SearchRes<CommunityQuestionMyRes> res = communityQuestionService.searchQuestionsByUserId(req, 1L);

        assertThat(res).isNotNull();
        assertThat(res.items()).hasSize(4);
        assertThat(res.items().stream().allMatch(q -> q.user().id().equals(1L))).isTrue();
        
        // 최신순 정렬
        CommunityQuestionMyRes question1 = res.items().get(0); // 질문2
        CommunityQuestionMyRes question2 = res.items().get(1); // 질문1
        CommunityQuestionMyRes question3 = res.items().get(2); // 질문6
        CommunityQuestionMyRes question4 = res.items().get(3); // 질문7
        
        // 답변 수 검증
        assertThat(question1.answerCount()).isEqualTo(1);
        assertThat(question2.answerCount()).isEqualTo(3);
        assertThat(question3.answerCount()).isEqualTo(2);
        assertThat(question4.answerCount()).isEqualTo(1);
        
        // 새 답변 여부 검증
        assertThat(question1.hasNewAnswer()).isTrue();   // 조회 기록 없음
        assertThat(question2.hasNewAnswer()).isTrue();   // 조회 후 새 답변 있음
        assertThat(question3.hasNewAnswer()).isFalse();  // 조회 후 새 답변 없음
        assertThat(question4.hasNewAnswer()).isFalse();  // 자기 답변만 있음
        
        assertThat(res.pagination().totalItems()).isEqualTo(4);
        assertThat(res.pagination().perPage()).isEqualTo(10);
        assertThat(res.pagination().currentPage()).isEqualTo(1);
    }



    /**
     * 코스별 질문 검색: courseId 필터를 검증한다.
     */
    @Test
    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) "
                    + "VALUES (1, 'user1@example.com', '사용자1', 1234, null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) "
                    + "VALUES (2, 'user2@example.com', '사용자2', 1234, null, 'LEARNER', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, thumbnail_id, is_show, created_at, updated_at, is_deleted) "
                    + "VALUES (1, 1, '코스1', '설명1', 10000, 0, 0, '대상', null, true, NOW(), NOW(), false);",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, thumbnail_id, is_show, created_at, updated_at, is_deleted) "
                    + "VALUES (2, 2, '코스2', '설명2', 20000, 0, 0, '대상', null, true, NOW(), NOW(), false);",

            // 코스1 질문 3개(1개 삭제)
            "INSERT INTO web_service.community_questions (id, user_id, course_id, title, content, status, created_at, updated_at, is_deleted) "
                    + "VALUES (1, 1, 1, 'C1-Q1', '내용1', 'ANSWERED', DATEADD('MINUTE', -30, NOW()), NOW(), false);",
            "INSERT INTO web_service.community_questions (id, user_id, course_id, title, content, status, created_at, updated_at, is_deleted) "
                    + "VALUES (2, 1, 1, 'C1-Q2', '내용2', 'WAITING', DATEADD('MINUTE', -20, NOW()), NOW(), false);",
            "INSERT INTO web_service.community_questions (id, user_id, course_id, title, content, status, created_at, updated_at, is_deleted) "
                    + "VALUES (3, 1, 1, 'C1-Q3', '내용3', 'ACCEPTED', DATEADD('MINUTE', -10, NOW()), NOW(), true);",

            // 코스2 질문 2개(필터로 제외)
            "INSERT INTO web_service.community_questions (id, user_id, course_id, title, content, status, created_at, updated_at, is_deleted) "
                    + "VALUES (4, 2, 2, 'C2-Q1', '내용4', 'ANSWERED', DATEADD('MINUTE', -5, NOW()), NOW(), false);",
            "INSERT INTO web_service.community_questions (id, user_id, course_id, title, content, status, created_at, updated_at, is_deleted) "
                    + "VALUES (5, 2, 2, 'C2-Q2', '내용5', 'WAITING', DATEADD('MINUTE', -15, NOW()), NOW(), false);"})
    void searchQuestionsByCourseId_정상() {
        CommunityQuestionSearchReq req = new CommunityQuestionSearchReq(1, 10, null, null, null, null, null);

        SearchRes<CommunityQuestionRes> res = communityQuestionService.searchQuestionsByCourseId(req, 1L);

        assertThat(res).isNotNull();
        assertThat(res.items()).hasSize(2);
        assertThat(res.items().stream().allMatch(q -> q.courseId().equals(1L))).isTrue();
        assertThat(res.pagination().totalItems()).isEqualTo(2);
        assertThat(res.pagination().perPage()).isEqualTo(10);
        assertThat(res.pagination().currentPage()).isEqualTo(1);
    }

    /**
     * 질문 상세 조회: 첨부파일/비디오/답변 목록을 검증한다.
     */
    @Test
    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) "
                    + "VALUES (1, 'author@example.com', '질문작성자', 1234, null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) "
                    + "VALUES (2, 'replier@example.com', '답변자', 1234, null, 'LEARNER', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, thumbnail_id, is_show, created_at, updated_at, is_deleted) "
                    + "VALUES (1, 1, '상세코스', '상세설명', 10000, 0, 0, '대상', null, true, NOW(), NOW(), false);",

            "INSERT INTO web_service.community_questions (id, user_id, course_id, title, content, status, created_at, updated_at, is_deleted) "
                    + "VALUES (1, 1, 1, '상세 질문 제목', '상세 질문 내용', 'ANSWERED', NOW(), NOW(), false);",

            "INSERT INTO web_service.files (id, container_id, container_type, content_type, name, original_name, size, created_at, updated_at) "
                    + "VALUES (100, 1, 'QUESTION_IMAGE', 'image/jpeg', 'detail_q_attachment1.jpg', 'detail_q_attachment1.jpg', 1024, NOW(), NOW());",
            "INSERT INTO web_service.files (id, container_id, container_type, content_type, name, original_name, size, created_at, updated_at) "
                    + "VALUES (101, 1, 'QUESTION_IMAGE', 'application/pdf', 'detail_q_attachment2.pdf', 'detail_q_attachment2.pdf', 2048, NOW(), NOW());",
            "INSERT INTO web_service.community_question_files (question_id, file_id, created_at, updated_at) VALUES (1, 100, NOW(), NOW());",
            "INSERT INTO web_service.community_question_files (question_id, file_id, created_at, updated_at) VALUES (1, 101, NOW(), NOW());",

            "INSERT INTO web_service.video_questions (id, video_uuid, community_question_id, user_id, s3key, extension, original_file_name, duration, encoding_status, encoding_at, created_at, updated_at, is_deleted) "
                    + "VALUES (10, '00000000-0000-0000-0000-000000000010', 1, 1, 'vod/QUESTION/mp4/00000000-0000-0000-0000-000000000010/detail_question_video.mp4', 'mp4', 'detail_question_video.mp4', 25, 'COMPLETED', NOW(), NOW(), NOW(), false);",

            "INSERT INTO web_service.community_answers (id, user_id, question_id, content, is_accepted, created_at, updated_at, is_deleted) "
                    + "VALUES (1, 2, 1, '상세 답변1 (비디오 포함)', false, DATEADD('MINUTE', -10, NOW()), NOW(), false);",
            "INSERT INTO web_service.community_answers (id, user_id, question_id, content, is_accepted, created_at, updated_at, is_deleted) "
                    + "VALUES (2, 1, 1, '상세 답변2 (첨부파일 포함)', true, DATEADD('MINUTE', -5, NOW()), NOW(), false);",
            "INSERT INTO web_service.video_answers (id, video_uuid, community_answer_id, user_id, s3key, extension, original_file_name, duration, encoding_status, encoding_at, created_at, updated_at, is_deleted) "
                    + "VALUES (20, '00000000-0000-0000-0000-000000000020', 1, 2, 'vod/ANSWER/mp4/00000000-0000-0000-0000-000000000020/detail_answer_video.mp4', 'mp4', 'detail_answer_video.mp4', 30, 'COMPLETED', NOW(), NOW(), NOW(), false);",
            "INSERT INTO web_service.files (id, container_id, container_type, content_type, name, original_name, size, created_at, updated_at) "
                    + "VALUES (200, 2, 'ANSWER_IMAGE', 'image/jpeg', 'detail_answer_attachment1.jpg', 'detail_answer_attachment1.jpg', 512, NOW(), NOW());",
            "INSERT INTO web_service.community_answers_files (answer_id, file_id, created_at, updated_at) VALUES (2, 200, NOW(), NOW());"})
    void getQuestionDetails_정상() {
        when(appProperties.getDomain()).thenReturn("insty.test.com");

        CommunityQuestionDetailsRes res = communityQuestionService.getQuestionDetails(1L, 1L);

        assertThat(res).isNotNull();
        assertThat(res.user().id()).isEqualTo(1L);
        assertThat(res.user().nickname()).isEqualTo("질문작성자");
        assertThat(res.user().userType()).isEqualTo(UserType.CREATOR);
        assertThat(res.courseId()).isEqualTo(1L);
        assertThat(res.title()).isEqualTo("상세 질문 제목");
        assertThat(res.content()).isEqualTo("상세 질문 내용");

        assertThat(res.attachments()).hasSize(2);
        assertThat(res.attachments().get(0).name()).isEqualTo("detail_q_attachment1.jpg");
        assertThat(res.attachments().get(0).contentType()).isEqualTo("image/jpeg");
        assertThat(res.attachments().get(1).name()).isEqualTo("detail_q_attachment2.pdf");
        assertThat(res.attachments().get(1).contentType()).isEqualTo("application/pdf");

        assertThat(res.videoInfo()).isNotNull();
        assertThat(res.videoInfo().videoUuid()).isEqualTo(UUID.fromString("00000000-0000-0000-0000-000000000010"));
        assertThat(res.videoInfo().originFileName()).isEqualTo("detail_question_video.mp4");

    }

    /**
     * 질문 삭제: 연관 답변/파일/비디오 정리와 권한을 검증한다.
     */
    @Test
    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) "
                    + "VALUES (1, 'author@example.com', '질문작성자', 1234, null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) "
                    + "VALUES (2, 'replier@example.com', '답변자', 1234, null, 'LEARNER', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, thumbnail_id, is_show, created_at, updated_at, is_deleted) "
                    + "VALUES (1, 1, '삭제코스', '삭제설명', 10000, 0, 0, '대상', null, true, NOW(), NOW(), false);",

            // 질문 및 파일
            "INSERT INTO web_service.community_questions (id, user_id, course_id, title, content, status, created_at, updated_at, is_deleted) "
                    + "VALUES (1, 1, 1, '삭제 대상 질문', '삭제 대상 질문 내용', 'ANSWERED', NOW(), NOW(), false);",
            "INSERT INTO web_service.files (id, container_id, container_type, content_type, name, original_name, size, created_at, updated_at) "
                    + "VALUES (100, 1, 'QUESTION_IMAGE', 'image/jpeg', 'del_q_attachment1.jpg', 'del_q_attachment1.jpg', 1024, NOW(), NOW());",
            "INSERT INTO web_service.community_question_files (question_id, file_id, created_at, updated_at) VALUES (1, 100, NOW(), NOW());",

            // 질문 비디오
            "INSERT INTO web_service.video_questions (id, video_uuid, community_question_id, user_id, s3key, extension, original_file_name, duration, encoding_status, encoding_at, created_at, updated_at, is_deleted) "
                    + "VALUES (10, '00000000-0000-0000-0000-000000000010', 1, 1, 'vod/QUESTION/mp4/00000000-0000-0000-0000-000000000010/del_question_video.mp4', 'mp4', 'del_question_video.mp4', 25, 'COMPLETED', NOW(), NOW(), NOW(), false);",

            // 답변 2개 + 각 파일/비디오
            "INSERT INTO web_service.community_answers (id, user_id, question_id, content, is_accepted, created_at, updated_at, is_deleted) "
                    + "VALUES (1, 2, 1, '삭제 대상 답변1', false, DATEADD('MINUTE', -10, NOW()), NOW(), false);",
            "INSERT INTO web_service.community_answers (id, user_id, question_id, content, is_accepted, created_at, updated_at, is_deleted) "
                    + "VALUES (2, 2, 1, '삭제 대상 답변2', true, DATEADD('MINUTE', -5, NOW()), NOW(), false);",
            "INSERT INTO web_service.files (id, container_id, container_type, content_type, name, original_name, size, created_at, updated_at) "
                    + "VALUES (200, 1, 'ANSWER_IMAGE', 'image/jpeg', 'del_answer_attachment1.jpg', 'del_answer_attachment1.jpg', 512, NOW(), NOW());",
            "INSERT INTO web_service.community_answers_files (answer_id, file_id, created_at, updated_at) VALUES (1, 200, NOW(), NOW());",
            "INSERT INTO web_service.video_answers (id, video_uuid, community_answer_id, user_id, s3key, extension, original_file_name, duration, encoding_status, encoding_at, created_at, updated_at, is_deleted) "
                    + "VALUES (20, '00000000-0000-0000-0000-000000000020', 2, 2, 'vod/ANSWER/mp4/00000000-0000-0000-0000-000000000020/del_answer_video.mp4', 'mp4', 'del_answer_video.mp4', 30, 'COMPLETED', NOW(), NOW(), NOW(), false);"})
    void deleteQuestion_정상() {
        Long userId = 1L;
        Long questionId = 1L;

        var before = communityQuestionReader.getCommunityQuestionWithAnswerById(questionId);
        assertThat(before.getAnswers()).hasSize(2);
        assertThat(communityQuestionVideoManager.getVideoQuestion(before)).isNotNull();

        when(videoEncodingRepository.findByVideoUuid(any())).thenReturn(Optional.of(mock(insty.model.video.VideoEncoding.class)));

        communityQuestionService.deleteQuestion(userId, questionId);

        assertThatThrownBy(() -> communityQuestionReader.getCommunityQuestionWithAnswerById(questionId)).isInstanceOf(
                insty.exception.CustomException.class);
        assertThatThrownBy(() -> communityAnswerReader.getCommunityAnswerById(1L)).isInstanceOf(
                insty.exception.CustomException.class);
        assertThatThrownBy(() -> communityAnswerReader.getCommunityAnswerById(2L)).isInstanceOf(
                insty.exception.CustomException.class);
    }

    /**
     * 질문 작성자와 강의 개시자의 조회 기록 업데이트 테스트
     */
    @Test
    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, user_type, is_deleted, is_email_agreed, created_at, updated_at) VALUES (1, 'question_author@test.com', '질문작성자', 'password', 'LEARNER', false, true, NOW(), NOW())",
            "INSERT INTO web_service.users (id, email, nickname, password, user_type, is_deleted, is_email_agreed, created_at, updated_at) VALUES (2, 'course_creator@test.com', '강의제작자', 'password', 'CREATOR', false, true, NOW(), NOW())",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, is_show, created_at, updated_at, is_deleted) VALUES (1, 2, '테스트 강의', '테스트 설명', 10000, 0, 0, true, NOW(), NOW(), false)",
            "INSERT INTO web_service.community_questions (id, user_id, course_id, title, content, status, created_at, updated_at, is_deleted) VALUES (1, 1, 1, '테스트 질문', '테스트 내용', 'WAITING', NOW(), NOW(), false)"
    })
    void recordQuestionViewIfAuthorOrCreator_정상() throws InterruptedException {
        // given
        Long questionId = 1L;
        Long questionAuthorId = 1L;
        Long courseCreatorId = 2L;
        Long otherUserId = 999L;

        // when - 질문 작성자가 조회
        communityQuestionService.getQuestionDetails(questionId, questionAuthorId);

        // then - 질문 작성자의 조회 기록이 생성되었는지 확인
        CommunityQuestionView authorView = communityQuestionViewRepository.findByQuestionIdAndUserId(questionId, questionAuthorId).orElse(null);
        assertThat(authorView).isNotNull();
        assertThat(authorView.getCommunityQuestion().getId()).isEqualTo(questionId);
        assertThat(authorView.getCommunityQuestionViewId().getUserId()).isEqualTo(questionAuthorId);
        assertThat(authorView.getLastViewedAt()).isNotNull();

        // when - 강의 개시자가 조회
        Thread.sleep(100); // 시간 차이를 위해 잠시 대기
        communityQuestionService.getQuestionDetails(questionId, courseCreatorId);

        // then - 강의 개시자의 조회 기록이 생성되었는지 확인
        CommunityQuestionView creatorView = communityQuestionViewRepository.findByQuestionIdAndUserId(questionId, courseCreatorId).orElse(null);
        assertThat(creatorView).isNotNull();
        assertThat(creatorView.getCommunityQuestion().getId()).isEqualTo(questionId);
        assertThat(creatorView.getCommunityQuestionViewId().getUserId()).isEqualTo(courseCreatorId);
        assertThat(creatorView.getLastViewedAt()).isAfter(authorView.getLastViewedAt());

        // when - 다른 사용자가 조회
        Thread.sleep(100); // 시간 차이를 위해 잠시 대기
        communityQuestionService.getQuestionDetails(questionId, otherUserId);

        // then - 다른 사용자의 조회 기록은 생성되지 않았는지 확인
        CommunityQuestionView otherView = communityQuestionViewRepository.findByQuestionIdAndUserId(questionId, otherUserId).orElse(null);
        assertThat(otherView).isNull();

        // then - 기존 조회 기록들은 그대로 유지되는지 확인
        CommunityQuestionView authorViewAfter = communityQuestionViewRepository.findByQuestionIdAndUserId(questionId, questionAuthorId).orElse(null);
        CommunityQuestionView creatorViewAfter = communityQuestionViewRepository.findByQuestionIdAndUserId(questionId, courseCreatorId).orElse(null);
        assertThat(authorViewAfter).isNotNull();
        assertThat(creatorViewAfter).isNotNull();
    }
}


