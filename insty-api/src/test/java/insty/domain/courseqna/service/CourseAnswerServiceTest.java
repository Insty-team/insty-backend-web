package insty.domain.courseqna.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import insty.ai.adapter.AiRequester;
import insty.cloudfront.adapter.CloudFrontSigner;
import insty.domain.common.FileInfo;
import insty.domain.courseqna.dto.CourseQnaAcceptAnswerResultRes;
import insty.domain.courseqna.dto.CourseAnswerCreateReq;
import insty.domain.courseqna.dto.CourseAnswerRes;
import insty.domain.courseqna.dto.CourseAnswerUpdateReq;
import insty.domain.courseqna.implement.CourseAnswerAcceptManager;
import insty.domain.courseqna.implement.CourseAnswerFileReader;
import insty.domain.courseqna.implement.CourseAnswerFileWriter;
import insty.domain.courseqna.implement.CourseAnswerMapper;
import insty.domain.courseqna.implement.CourseAnswerReader;
import insty.domain.courseqna.implement.CourseAnswerVideoManager;
import insty.domain.courseqna.implement.CourseAnswerWriter;
import insty.domain.courseqna.implement.CourseQuestionReader;
import insty.domain.courseqna.implement.CourseQuestionStatusManager;
import insty.domain.courseqna.implement.CourseQnaValidator;
import insty.domain.courseqna.repository.CourseQuestionRepository;
import insty.domain.user.implement.UserReader;
import insty.error.CommunityErrorCode;
import insty.exception.CustomException;
import insty.global.property.AppProperties;
import insty.model.courseqna.CourseAnswer;
import insty.model.courseqna.CourseQuestion;
import insty.model.courseqna.QuestionStatus;
import insty.model.user.UserType;
import insty.model.video.VideoAnswer;
import insty.model.video.VideoType;
import insty.s3.adapter.S3FileManager;
import insty.s3.adapter.S3UrlIssuer;
import static org.mockito.Mockito.mock;
import insty.domain.video.repository.VideoEncodingRepository;

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
class CourseAnswerServiceTest {

    @Autowired
    private CourseAnswerService courseAnswerService;

    @Autowired
    private CourseAnswerReader communityAnswerReader;
    @Autowired
    private CourseAnswerWriter courseAnswerWriter;
    @Autowired
    private CourseAnswerFileReader communityAnswerFileReader;
    @Autowired
    private CourseAnswerFileWriter courseAnswerFileWriter;
    @Autowired
    private CourseAnswerVideoManager courseAnswerVideoManager;
    @Autowired
    private CourseAnswerAcceptManager communityAnswerAcceptManager;
    @Autowired
    private CourseQnaValidator courseQnaValidator;
    @Autowired
    private CourseAnswerMapper communityAnswerMapper;
    @Autowired
    private CourseQuestionReader courseQuestionReader;
    @Autowired
    private CourseQuestionStatusManager communityQuestionStatusManager;
    @Autowired
    private CourseQuestionRepository courseQuestionRepository;
    @Autowired
    private UserReader userReader;

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
    @MockitoBean
    private VideoEncodingRepository videoEncodingRepository;


    /**
     * 답변 생성: 비디오/첨부 파일 유무에 따른 저장을 검증한다.
     */
    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) "
                    + "VALUES (1, 'question_author@example.com', '질문작성자', 1234, null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) "
                    + "VALUES (2, 'course_creator@example.com', '강의제작자', 1234, null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, thumbnail_id, is_show, created_at, updated_at, is_deleted) "
                    + "VALUES (1, 2, '테스트 강의', '테스트 강의 설명', 20000, 0, 0, '테스트 대상자', null, true, NOW(), NOW(), false);",
            "INSERT INTO web_service.community_questions (id, user_id, course_id, title, content, status, created_at, updated_at, is_deleted) "
                    + "VALUES (1, 2, 1, '테스트 질문', '테스트 질문 내용', 'WAITING', NOW(), NOW(), false);",
            "INSERT INTO web_service.video_answers (id, video_uuid, community_answer_id, user_id, s3key, extension, original_file_name, duration, encoding_status, encoding_at, created_at, updated_at, is_deleted) "
                    + "VALUES (1, '00000000-0000-0000-0000-000000000001', null, 1, 'vod/ANSWER/mp4/00000000-0000-0000-0000-000000000001/answer_video.mp4', 'mp4', 'answer_video.mp4', 10, 'PROCESSING', NOW(), NOW(), NOW(), false)"})
    @Test
    void saveAnswer_정상() {
        Long questionId = 1L;
        Long questionAuthorId = 1L;
        Long courseCreatorId = 2L;

        String firstAnswerContent = "첫 번째 답변 내용입니다.";
        UUID firstVideoUuid = UUID.fromString("00000000-0000-0000-0000-000000000001");
        CourseAnswerCreateReq firstReq = new CourseAnswerCreateReq(firstAnswerContent, firstVideoUuid);
        List<MultipartFile> firstAttachments = List.of(
                new MockMultipartFile("attachment", "test1.jpg", "image/jpeg", "content1".getBytes()));

        String secondAnswerContent = "두 번째 답변 내용입니다.";
        CourseAnswerCreateReq secondReq = new CourseAnswerCreateReq(secondAnswerContent, null);
        List<MultipartFile> secondAttachments = List.of();

        when(appProperties.getDomain()).thenReturn("insty.test.com");
        when(s3FileManager.upload(any(), anyString(), anyString())).thenReturn(
                "00000000-0000-0000-0000-000000000001.jpg");

        CourseAnswerRes firstRes = courseAnswerService.saveAnswer(questionAuthorId, questionId, firstReq,
                firstAttachments);

        assertThat(firstRes).isNotNull();
        assertThat(firstRes.content()).isEqualTo(firstAnswerContent);
        assertThat(firstRes.user().id()).isEqualTo(questionAuthorId);
        assertThat(firstRes.user().nickname()).isEqualTo("질문작성자");
        assertThat(firstRes.user().userType()).isEqualTo(UserType.CREATOR);
        assertThat(firstRes.isAccepted()).isFalse();
        assertThat(firstRes.attachments()).isNotNull();
        assertThat(firstRes.videoInfo()).isNotNull();
        assertThat(firstRes.videoInfo().videoUuid()).isEqualTo(firstVideoUuid);
        assertThat(firstRes.videoInfo().originFileName()).isEqualTo("answer_video.mp4");

        CourseQuestion question = courseQuestionReader.getCommunityQuestionWithAnswerById(questionId);
        assertThat(question.getStatus()).isEqualTo(QuestionStatus.ANSWERED);

        CourseAnswerRes secondRes = courseAnswerService.saveAnswer(questionAuthorId, questionId, secondReq,
                secondAttachments);

        assertThat(secondRes).isNotNull();
        assertThat(secondRes.content()).isEqualTo(secondAnswerContent);
        assertThat(secondRes.user().id()).isEqualTo(questionAuthorId);
        assertThat(secondRes.user().nickname()).isEqualTo("질문작성자");
        assertThat(secondRes.user().userType()).isEqualTo(UserType.CREATOR);
        assertThat(secondRes.isAccepted()).isFalse();
        assertThat(secondRes.attachments()).isEmpty();
        assertThat(secondRes.videoInfo()).isNull();

        question = courseQuestionReader.getCommunityQuestionWithAnswerById(questionId);
        assertThat(question.getStatus()).isEqualTo(QuestionStatus.ANSWERED);

        List<CourseAnswerRes> allAnswers = courseAnswerService.getAllAnswersByQuestionId(questionId);
        assertThat(allAnswers).hasSize(2);

        List<String> answerContents = allAnswers.stream().map(CourseAnswerRes::content).toList();
        assertThat(answerContents).containsExactlyInAnyOrder(firstAnswerContent, secondAnswerContent);

        CourseAnswerRes videoAnswer = allAnswers.stream().filter(answer -> answer.videoInfo() != null).findFirst()
                .orElse(null);
        assertThat(videoAnswer).isNotNull();
        assertThat(videoAnswer.content()).isEqualTo(firstAnswerContent);
        assertThat(videoAnswer.videoInfo().videoUuid()).isEqualTo(firstVideoUuid);
        CourseAnswerRes nonVideoAnswer = allAnswers.stream().filter(answer -> answer.videoInfo() == null).findFirst()
                .orElse(null);
        assertThat(nonVideoAnswer).isNotNull();
        assertThat(nonVideoAnswer.content()).isEqualTo(secondAnswerContent);
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) "
                    + "VALUES (1, 'answer_author@example.com', '답변작성자', 1234, null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, thumbnail_id, is_show, created_at, updated_at, is_deleted) "
                    + "VALUES (1, 1, '테스트 강의', '테스트 강의 설명', 20000, 0, 0, '테스트 대상자', null, true, NOW(), NOW(), false);",
            "INSERT INTO web_service.community_questions (id, user_id, course_id, title, content, status, created_at, updated_at, is_deleted) "
                    + "VALUES (1, 1, 1, '테스트 질문', '테스트 질문 내용', 'ANSWERED', NOW(), NOW(), false);",
            "INSERT INTO web_service.community_answers (id, user_id, question_id, content, is_accepted, created_at, updated_at, is_deleted) "
                    + "VALUES (1, 1, 1, '기존 답변 내용', false, NOW(), NOW(), false);",
            "INSERT INTO web_service.files (container_id, container_type, content_type, name, original_name, size, created_at, updated_at) "
                    + "VALUES (1, 'ANSWER_IMAGE', 'image/jpeg', 'old_attachment1.jpg', 'old_attachment1.jpg', 1024, NOW(), NOW());",
            "INSERT INTO web_service.community_answers_files (answer_id, file_id, created_at, updated_at) "
                    + "VALUES (1, (SELECT id FROM web_service.files WHERE name = 'old_attachment1.jpg'), NOW(), NOW());",
            "INSERT INTO web_service.video_answers (id, video_uuid, community_answer_id, user_id, s3key, extension, original_file_name, duration, encoding_status, encoding_at, created_at, updated_at, is_deleted) "
                    + "VALUES (1, '00000000-0000-0000-0000-000000000001', 1, 1, 'vod/ANSWER/mp4/00000000-0000-0000-0000-000000000001/old_answer_video.mp4', 'mp4', 'old_answer_video.mp4', 15, 'COMPLETED', NOW(), NOW(), NOW(), false);",
            "INSERT INTO web_service.video_answers (id, video_uuid, community_answer_id, user_id, s3key, extension, original_file_name, duration, encoding_status, encoding_at, created_at, updated_at, is_deleted) "
                    + "VALUES (2, '00000000-0000-0000-0000-000000000002', null, 1, 'vod/ANSWER/mp4/00000000-0000-0000-0000-000000000002/new_answer_video.mp4', 'mp4', 'new_answer_video.mp4', 20, 'COMPLETED', NOW(), NOW(), NOW(), false)"})
    @Test
    void updateAnswer_정상() {
        // given
        Long userId = 1L;
        Long answerId = 1L;
        String updatedContent = "수정된 답변 내용입니다.";
        UUID newVideoUuid = UUID.fromString("00000000-0000-0000-0000-000000000002");

        // 수정 전 기존 파일들 확인
        CourseAnswer answerBeforeUpdate = communityAnswerReader.getCommunityAnswerById(answerId);
        List<FileInfo> filesBeforeUpdate = communityAnswerFileReader.getAnswerFileInfos(answerBeforeUpdate);
        assertThat(filesBeforeUpdate).hasSize(1);

        // 기존 첨부파일 ID들 (삭제할 파일들) - 실제 파일 ID 사용
        List<Long> deleteFileIds = filesBeforeUpdate.stream().map(FileInfo::id).toList();

        CourseAnswerUpdateReq req = new CourseAnswerUpdateReq(updatedContent, newVideoUuid, deleteFileIds);

        // 새로운 첨부파일들
        List<MultipartFile> newAttachments = List.of(
                new MockMultipartFile("attachment1", "new_attachment1.jpg", "image/jpeg", "new_content1".getBytes()));

        // mock
        when(appProperties.getDomain()).thenReturn("insty.test.com");
        when(s3FileManager.upload(any(), anyString(), anyString())).thenReturn("new_attachment1.jpg")
                .thenReturn("new_attachment2.png");
        when(videoEncodingRepository.findByVideoUuid(any())).thenReturn(Optional.of(mock(insty.model.video.VideoEncoding.class)));

        // when
        CourseAnswerRes res = courseAnswerService.updateAnswer(userId, answerId, req, newAttachments);

        // then
        assertThat(res).isNotNull();

        // 내용 수정 검증
        assertThat(res.content()).isEqualTo(updatedContent);
        assertThat(res.user().id()).isEqualTo(userId);
        assertThat(res.user().nickname()).isEqualTo("답변작성자");
        assertThat(res.user().userType()).isEqualTo(UserType.CREATOR);
        assertThat(res.isAccepted()).isFalse();

        // 비디오 수정 검증 (기존 비디오에서 새로운 비디오로 변경)
        assertThat(res.videoInfo()).isNotNull();
        assertThat(res.videoInfo().videoUuid()).isEqualTo(newVideoUuid);
        assertThat(res.videoInfo().originFileName()).isEqualTo("new_answer_video.mp4");

        // 첨부파일 수정 검증 (기존 파일들이 삭제되고 새로운 파일들로 교체)
        assertThat(res.attachments()).hasSize(1);
        assertThat(res.attachments().get(0).name()).isEqualTo("new_attachment1.jpg");
        assertThat(res.attachments().get(0).contentType()).isEqualTo("image/jpeg");
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) "
                    + "VALUES (1, 'user1@example.com', '사용자1', 1234, null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) "
                    + "VALUES (2, 'user2@example.com', '사용자2', 1234, null, 'LEARNER', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, thumbnail_id, is_show, created_at, updated_at, is_deleted) "
                    + "VALUES (1, 1, '테스트 강의', '테스트 강의 설명', 20000, 0, 0, '테스트 대상자', null, true, NOW(), NOW(), false);",
            "INSERT INTO web_service.community_questions (id, user_id, course_id, title, content, status, created_at, updated_at, is_deleted) "
                    + "VALUES (1, 1, 1, '질문A', '질문A 내용', 'ACCEPTED', NOW(), NOW(), false);",
            "INSERT INTO web_service.community_questions (id, user_id, course_id, title, content, status, created_at, updated_at, is_deleted) "
                    + "VALUES (2, 1, 1, '질문B', '질문B 내용', 'WAITING', NOW(), NOW(), false);",
            "INSERT INTO web_service.community_answers (id, user_id, question_id, content, is_accepted, created_at, updated_at, is_deleted) "
                    + "VALUES (1, 1, 1, '질문A의 첫 번째 답변 (채택됨)', true, DATEADD('MINUTE', -30, NOW()), NOW(), false);",
            "INSERT INTO web_service.community_answers (id, user_id, question_id, content, is_accepted, created_at, updated_at, is_deleted) "
                    + "VALUES (2, 2, 1, '질문A의 두 번째 답변 (비디오 포함)', false, DATEADD('MINUTE', -20, NOW()), NOW(), false);",
            "INSERT INTO web_service.community_answers (id, user_id, question_id, content, is_accepted, created_at, updated_at, is_deleted) "
                    + "VALUES (3, 1, 1, '질문A의 세 번째 답변 (첨부파일 포함)', false, DATEADD('MINUTE', -10, NOW()), NOW(), false);",
            "INSERT INTO web_service.community_answers (id, user_id, question_id, content, is_accepted, created_at, updated_at, is_deleted) "
                    + "VALUES (4, 2, 2, '질문B의 답변 (다른 질문)', false, NOW(), NOW(), false);",
            "INSERT INTO web_service.files (container_id, container_type, content_type, name, original_name, size, created_at, updated_at) "
                    + "VALUES (3, 'ANSWER_IMAGE', 'image/jpeg', 'attachment1.jpg', 'attachment1.jpg', 1024, NOW(), NOW());",
            "INSERT INTO web_service.community_answers_files (answer_id, file_id, created_at, updated_at) "
                    + "VALUES (3, (SELECT id FROM web_service.files WHERE name = 'attachment1.jpg'), NOW(), NOW());",
            "INSERT INTO web_service.video_answers (id, video_uuid, community_answer_id, user_id, s3key, extension, original_file_name, duration, encoding_status, encoding_at, created_at, updated_at, is_deleted) "
                    + "VALUES (1, '00000000-0000-0000-0000-000000000001', 2, 2, 'vod/ANSWER/mp4/00000000-0000-0000-0000-000000000001/answer_video.mp4', 'mp4', 'answer_video.mp4', 15, 'COMPLETED', NOW(), NOW(), NOW(), false);"})
    @Test
    void getAllAnswersByQuestionId_정상() {
        // given
        Long questionId = 1L;

        // when
        List<CourseAnswerRes> res = courseAnswerService.getAllAnswersByQuestionId(questionId);

        // then
        assertThat(res).isNotNull();
        assertThat(res).hasSize(3);

        // 생성일 기준 최신순 정렬 검증 (최신 -> 오래된 순)
        assertThat(res.get(0).content()).isEqualTo("질문A의 세 번째 답변 (첨부파일 포함)");
        assertThat(res.get(1).content()).isEqualTo("질문A의 두 번째 답변 (비디오 포함)");
        assertThat(res.get(2).content()).isEqualTo("질문A의 첫 번째 답변 (채택됨)");

        // 첫 번째 답변 (최신, 첨부파일 포함)
        assertThat(res.get(0).user().id()).isEqualTo(1L);
        assertThat(res.get(0).user().nickname()).isEqualTo("사용자1");
        assertThat(res.get(0).user().userType()).isEqualTo(UserType.CREATOR);
        assertThat(res.get(0).isAccepted()).isFalse();
        assertThat(res.get(0).attachments()).hasSize(1);
        assertThat(res.get(0).attachments().get(0).name()).isEqualTo("attachment1.jpg");
        assertThat(res.get(0).attachments().get(0).contentType()).isEqualTo("image/jpeg");
        assertThat(res.get(0).videoInfo()).isNull();

        // 두 번째 답변 (비디오 포함)
        assertThat(res.get(1).user().id()).isEqualTo(2L);
        assertThat(res.get(1).user().nickname()).isEqualTo("사용자2");
        assertThat(res.get(1).user().userType()).isEqualTo(UserType.LEARNER);
        assertThat(res.get(1).isAccepted()).isFalse();
        assertThat(res.get(1).attachments()).isEmpty();
        assertThat(res.get(1).videoInfo()).isNotNull();
        assertThat(res.get(1).videoInfo().videoUuid()).isEqualTo(
                UUID.fromString("00000000-0000-0000-0000-000000000001"));
        assertThat(res.get(1).videoInfo().originFileName()).isEqualTo("answer_video.mp4");

        // 세 번째 답변 (채택됨)
        assertThat(res.get(2).user().id()).isEqualTo(1L);
        assertThat(res.get(2).user().nickname()).isEqualTo("사용자1");
        assertThat(res.get(2).user().userType()).isEqualTo(UserType.CREATOR);
        assertThat(res.get(2).isAccepted()).isTrue();
        assertThat(res.get(2).attachments()).isEmpty();
        assertThat(res.get(2).videoInfo()).isNull();
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) "
                    + "VALUES (1, 'answer_author@example.com', '답변작성자', 1234, null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, thumbnail_id, is_show, created_at, updated_at, is_deleted) "
                    + "VALUES (1, 1, '테스트 강의', '테스트 강의 설명', 20000, 0, 0, '테스트 대상자', null, true, NOW(), NOW(), false);",
            "INSERT INTO web_service.community_questions (id, user_id, course_id, title, content, status, created_at, updated_at, is_deleted) "
                    + "VALUES (1, 1, 1, '테스트 질문', '테스트 질문 내용', 'ANSWERED', NOW(), NOW(), false);",
            "INSERT INTO web_service.community_answers (id, user_id, question_id, content, is_accepted, created_at, updated_at, is_deleted) "
                    + "VALUES (1, 1, 1, '상세 조회할 답변 내용입니다.', true, NOW(), NOW(), false);",
            "INSERT INTO web_service.files (container_id, container_type, content_type, name, original_name, size, created_at, updated_at) "
                    + "VALUES (1, 'ANSWER_IMAGE', 'image/jpeg', 'detail_attachment1.jpg', 'detail_attachment1.jpg', 1024, NOW(), NOW());",
            "INSERT INTO web_service.files (container_id, container_type, content_type, name, original_name, size, created_at, updated_at) "
                    + "VALUES (1, 'ANSWER_IMAGE', 'application/pdf', 'detail_attachment2.pdf', 'detail_attachment2.pdf', 2048, NOW(), NOW());",
            "INSERT INTO web_service.community_answers_files (answer_id, file_id, created_at, updated_at) "
                    + "VALUES (1, (SELECT id FROM web_service.files WHERE name = 'detail_attachment1.jpg'), NOW(), NOW());",
            "INSERT INTO web_service.community_answers_files (answer_id, file_id, created_at, updated_at) "
                    + "VALUES (1, (SELECT id FROM web_service.files WHERE name = 'detail_attachment2.pdf'), NOW(), NOW());",
            "INSERT INTO web_service.video_answers (id, video_uuid, community_answer_id, user_id, s3key, extension, original_file_name, duration, encoding_status, encoding_at, created_at, updated_at, is_deleted) "
                    + "VALUES (1, '00000000-0000-0000-0000-000000000001', 1, 1, 'vod/ANSWER/mp4/00000000-0000-0000-0000-000000000001/detail_answer_video.mp4', 'mp4', 'detail_answer_video.mp4', 25, 'COMPLETED', NOW(), NOW(), NOW(), false);"})
    @Test
    void getAnswerDetails_정상() {
        // given
        Long answerId = 1L;

        // when
        CourseAnswerRes res = courseAnswerService.getAnswerDetails(answerId);

        // then
        assertThat(res).isNotNull();

        // 답변 내용 검증
        assertThat(res.content()).isEqualTo("상세 조회할 답변 내용입니다.");

        // 사용자 정보 검증
        assertThat(res.user().id()).isEqualTo(1L);
        assertThat(res.user().nickname()).isEqualTo("답변작성자");
        assertThat(res.user().userType()).isEqualTo(UserType.CREATOR);

        // 채택 여부 검증
        assertThat(res.isAccepted()).isTrue();

        // 첨부파일 검증
        assertThat(res.attachments()).hasSize(2);
        assertThat(res.attachments().get(0).name()).isEqualTo("detail_attachment1.jpg");
        assertThat(res.attachments().get(0).contentType()).isEqualTo("image/jpeg");
        assertThat(res.attachments().get(0).size()).isEqualTo(1024L);
        assertThat(res.attachments().get(1).name()).isEqualTo("detail_attachment2.pdf");
        assertThat(res.attachments().get(1).contentType()).isEqualTo("application/pdf");
        assertThat(res.attachments().get(1).size()).isEqualTo(2048L);

        // 비디오 정보 검증
        assertThat(res.videoInfo()).isNotNull();
        assertThat(res.videoInfo().videoUuid()).isEqualTo(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        assertThat(res.videoInfo().originFileName()).isEqualTo("detail_answer_video.mp4");
        assertThat(res.videoInfo().videoType()).isEqualTo(VideoType.ANSWER);
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) "
                    + "VALUES (1, 'answer_author@example.com', '답변작성자', 1234, null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, thumbnail_id, is_show, created_at, updated_at, is_deleted) "
                    + "VALUES (1, 1, '테스트 강의', '테스트 강의 설명', 20000, 0, 0, '테스트 대상자', null, true, NOW(), NOW(), false);",
            "INSERT INTO web_service.community_questions (id, user_id, course_id, title, content, status, created_at, updated_at, is_deleted) "
                    + "VALUES (1, 1, 1, '테스트 질문', '테스트 질문 내용', 'ANSWERED', NOW(), NOW(), false);",
            "INSERT INTO web_service.community_answers (id, user_id, question_id, content, is_accepted, created_at, updated_at, is_deleted) "
                    + "VALUES (1, 1, 1, '삭제할 답변 내용입니다.', true, NOW(), NOW(), false);",
            "INSERT INTO web_service.files (container_id, container_type, content_type, name, original_name, size, created_at, updated_at) "
                    + "VALUES (1, 'ANSWER_IMAGE', 'image/jpeg', 'delete_attachment1.jpg', 'delete_attachment1.jpg', 1024, NOW(), NOW());",
            "INSERT INTO web_service.files (container_id, container_type, content_type, name, original_name, size, created_at, updated_at) "
                    + "VALUES (1, 'ANSWER_IMAGE', 'application/pdf', 'delete_attachment2.pdf', 'delete_attachment2.pdf', 2048, NOW(), NOW());",
            "INSERT INTO web_service.community_answers_files (answer_id, file_id, created_at, updated_at) "
                    + "VALUES (1, (SELECT id FROM web_service.files WHERE name = 'delete_attachment1.jpg'), NOW(), NOW());",
            "INSERT INTO web_service.community_answers_files (answer_id, file_id, created_at, updated_at) "
                    + "VALUES (1, (SELECT id FROM web_service.files WHERE name = 'delete_attachment2.pdf'), NOW(), NOW());",
            "INSERT INTO web_service.video_answers (id, video_uuid, community_answer_id, user_id, s3key, extension, original_file_name, duration, encoding_status, encoding_at, created_at, updated_at, is_deleted) "
                    + "VALUES (1, '00000000-0000-0000-0000-000000000001', 1, 1, 'vod/ANSWER/mp4/00000000-0000-0000-0000-000000000001/delete_answer_video.mp4', 'mp4', 'delete_answer_video.mp4', 30, 'COMPLETED', NOW(), NOW(), NOW(), false);"})
    @Test
    void deleteAnswer_정상() {
        // given
        Long userId = 1L;
        Long answerId = 1L;
        Long questionId = 1L;

        // 삭제 전 상태 확인
        CourseAnswer answerBeforeDelete = communityAnswerReader.getCommunityAnswerById(answerId);
        assertThat(answerBeforeDelete.isDeleted()).isFalse();
        assertThat(answerBeforeDelete.isAccepted()).isTrue();

        List<FileInfo> filesBeforeDelete = communityAnswerFileReader.getAnswerFileInfos(answerBeforeDelete);
        assertThat(filesBeforeDelete).hasSize(2);

        VideoAnswer videoBeforeDelete = courseAnswerVideoManager.getVideoAnswer(answerBeforeDelete);
        assertThat(videoBeforeDelete).isNotNull();
        assertThat(videoBeforeDelete.isDeleted()).isFalse();

        when(videoEncodingRepository.findByVideoUuid(any())).thenReturn(Optional.of(mock(insty.model.video.VideoEncoding.class)));

        // when
        courseAnswerService.deleteAnswer(userId, answerId);

        // then
        assertThatThrownBy(() -> communityAnswerReader.getCommunityAnswerById(answerId)).isInstanceOf(
                CustomException.class);

        // 질문 상태가 올바르게 변경되었는지 확인 (모든 답변이 삭제되었으므로 WAITING)
        CourseQuestion question = courseQuestionReader.getCommunityQuestionWithAnswerById(questionId);
        assertThat(question.getStatus()).isEqualTo(QuestionStatus.WAITING);
        assertThat(question.getAcceptedAnswer()).isNull();

        // 질문의 답변 목록에서 삭제된 답변이 제외되었는지 확인
        List<CourseAnswerRes> remainingAnswers = courseAnswerService.getAllAnswersByQuestionId(questionId);
        assertThat(remainingAnswers).isEmpty();
    }

    /// ///
    /// acceptAnswer 통합 테스트 - 답변 채택의 경우 복잡한 로직이므로 상세하게 체크한다.
    /// ///

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) "
                    + "VALUES (1, 'user1@example.com', '사용자1', 1234, null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) "
                    + "VALUES (2, 'user2@example.com', '사용자2', 1234, null, 'LEARNER', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, thumbnail_id, is_show, created_at, updated_at, is_deleted) "
                    + "VALUES (1, 2, '테스트 강의', '테스트 강의 설명', 20000, 0, 0, '테스트 대상자', null, true, NOW(), NOW(), false);",
            "INSERT INTO web_service.community_questions (id, user_id, course_id, title, content, status, created_at, updated_at, is_deleted) "
                    + "VALUES (1, 2, 1, '테스트 질문', '테스트 질문 내용', 'ANSWERED', NOW(), NOW(), false);",
            "INSERT INTO web_service.community_answers (id, user_id, question_id, content, is_accepted, created_at, updated_at, is_deleted) "
                    + "VALUES (1, 1, 1, '답변1 - 사용자1이 작성', false, NOW(), NOW(), false);",
            "INSERT INTO web_service.community_answers (id, user_id, question_id, content, is_accepted, created_at, updated_at, is_deleted) "
                    + "VALUES (2, 2, 1, '답변2 - 사용자2(질문자)가 작성', false, NOW(), NOW(), false);",
            "INSERT INTO web_service.community_answers (id, user_id, question_id, content, is_accepted, created_at, updated_at, is_deleted) "
                    + "VALUES (3, 1, 1, '답변3 - 사용자1이 작성', false, NOW(), NOW(), false);"})
    @Test
    void acceptAnswer_종합테스트() {
        // given
        Long questionId = 1L;
        Long questionAuthorId = 2L; // 질문 작성자
        Long answer1Id = 1L; // 사용자1이 작성한 답변
        Long answer2Id = 2L; // 사용자2(질문자)가 작성한 답변
        Long answer3Id = 3L; // 사용자1이 작성한 답변

        // 시나리오 1: 답변1 채택 -> 성공
        CourseQnaAcceptAnswerResultRes res1 = courseAnswerService.acceptAnswer(questionAuthorId, questionId, answer1Id);
        assertThat(res1).isNotNull();
        assertThat(res1.accepted()).isTrue();

        // 질문 상태가 ACCEPTED로 변경되었는지 확인
        CourseQuestion question = courseQuestionReader.getCommunityQuestionWithAnswerById(questionId);
        assertThat(question.getStatus()).isEqualTo(QuestionStatus.ACCEPTED);
        assertThat(question.getAcceptedAnswer().getId()).isEqualTo(answer1Id);

        // 시나리오 2: 이미 채택된 상태에서 다른 답변 채택 시도 -> 실패 (409 에러)
        assertThatThrownBy(() -> courseAnswerService.acceptAnswer(questionAuthorId, questionId, answer3Id))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_ALREADY_ACCEPTED_ANSWER);

        // 시나리오 3: 채택된 답변을 다시 클릭 -> 채택 취소
        CourseQnaAcceptAnswerResultRes res3 = courseAnswerService.acceptAnswer(questionAuthorId, questionId, answer1Id);
        assertThat(res3).isNotNull();
        assertThat(res3.accepted()).isFalse();

        // 질문 상태가 ANSWERED로 변경되었는지 확인
        question = courseQuestionReader.getCommunityQuestionWithAnswerById(questionId);
        assertThat(question.getStatus()).isEqualTo(QuestionStatus.ANSWERED);
        assertThat(question.getAcceptedAnswer()).isNull();

        // 시나리오 4: 질문자가 아닌 사용자의 채택 시도 -> 실패
        assertThatThrownBy(() -> courseAnswerService.acceptAnswer(1L, questionId, answer1Id))
                .isInstanceOf(CustomException.class);

        // 시나리오 5: 질문자 본인 답변 채택 시도 -> 성공
        var res4 = courseAnswerService.acceptAnswer(questionAuthorId, questionId, answer2Id);
        assertThat(res4.accepted()).isTrue();
        assertThat(res4.answerId()).isEqualTo(answer2Id);
        
        // 질문 상태 확인
        question = courseQuestionReader.getCommunityQuestionWithAnswerById(questionId);
        assertThat(question.getStatus()).isEqualTo(QuestionStatus.ACCEPTED);
        assertThat(question.getAcceptedAnswer()).isNotNull();
        assertThat(question.getAcceptedAnswer().getId()).isEqualTo(answer2Id);
    }



    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) VALUES (1, 'runner@test.com', 'runner', 1234, null, 'LEARNER', false, null, false, NOW(), NOW(), NOW())",
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) VALUES (2, 'creator1@test.com', 'creator1', 1234, null, 'CREATOR', false, null, false, NOW(), NOW(), NOW())",
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) VALUES (3, 'creator2@test.com', 'creator2', 1234, null, 'CREATOR', false, null, false, NOW(), NOW(), NOW())",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, thumbnail_id, is_show, created_at, updated_at, is_deleted) VALUES (1, 2, '테스트 강의', '강의 설명', 20000, 0, 0, '테스트 대상자', null, true, NOW(), NOW(), false)",
            "INSERT INTO web_service.community_questions (id, user_id, course_id, title, content, status, created_at, updated_at, is_deleted) VALUES (1, 1, 1, '질문 제목', '질문 내용', 'WAITING', NOW(), NOW(), false)"
    })
    @Test
    void acceptAnswer_동적시나리오_종합테스트() {
        // given - mocks setup
        when(appProperties.getDomain()).thenReturn("insty.test.com");
        when(s3FileManager.upload(any(), anyString(), anyString())).thenReturn("00000000-0000-0000-0000-000000000001.jpg");

        // 시나리오 1: 첫 번째 답변 작성 및 채택
        CourseAnswerCreateReq firstAnswerReq = new CourseAnswerCreateReq("첫 번째 크리에이터 답변", null);
        var firstAnswerRes = courseAnswerService.saveAnswer(2L, 1L, firstAnswerReq, List.of());
        Long firstAnswerId = firstAnswerRes.answerId();

        CourseQnaAcceptAnswerResultRes acceptResult = courseAnswerService.acceptAnswer(1L, 1L, firstAnswerId);
        assertThat(acceptResult.accepted()).isTrue();

        CourseQuestion question = courseQuestionRepository.findById(1L).orElseThrow();
        assertThat(question.getStatus()).isEqualTo(QuestionStatus.ACCEPTED);
        assertThat(question.getAcceptedAnswer().getId()).isEqualTo(firstAnswerId);

        // 시나리오 2: 새 답변 작성 후에도 채택 상태 유지 확인
        CourseAnswerCreateReq secondAnswerReq = new CourseAnswerCreateReq("두 번째 크리에이터 답변", null);
        var secondAnswerRes = courseAnswerService.saveAnswer(3L, 1L, secondAnswerReq, List.of());
        Long secondAnswerId = secondAnswerRes.answerId();

        question = courseQuestionRepository.findById(1L).orElseThrow();
        assertThat(question.getStatus()).isEqualTo(QuestionStatus.ACCEPTED); // 여전히 ACCEPTED
        assertThat(question.getAcceptedAnswer().getId()).isEqualTo(firstAnswerId); // 첫 번째 답변이 여전히 채택됨

        // 시나리오 3: 이미 채택된 상태에서 다른 답변 채택 시도 -> 409 에러
        assertThatThrownBy(() -> courseAnswerService.acceptAnswer(1L, 1L, secondAnswerId))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_ALREADY_ACCEPTED_ANSWER);

        // 시나리오 4: 채택되지 않은 답변 삭제 -> 채택 상태 유지
        courseAnswerService.deleteAnswer(3L, secondAnswerId);
        question = courseQuestionRepository.findById(1L).orElseThrow();
        assertThat(question.getStatus()).isEqualTo(QuestionStatus.ACCEPTED); // 여전히 ACCEPTED
        assertThat(question.getAcceptedAnswer().getId()).isEqualTo(firstAnswerId); // 첫 번째 답변 여전히 채택됨

        // 시나리오 5: 채택된 답변 삭제 -> 채택 상태 해제
        courseAnswerService.deleteAnswer(2L, firstAnswerId);
        question = courseQuestionRepository.findById(1L).orElseThrow();
        assertThat(question.getStatus()).isEqualTo(QuestionStatus.WAITING); // 답변 없으므로 WAITING
        assertThat(question.getAcceptedAnswer()).isNull(); // 채택된 답변 없음
    }

    @Sql(statements = {
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) "
                    + "VALUES (1, 'user1@example.com', '사용자1', 1234, null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) "
                    + "VALUES (2, 'user2@example.com', '사용자2', 1234, null, 'LEARNER', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, thumbnail_id, is_show, created_at, updated_at, is_deleted) "
                    + "VALUES (1, 1, '테스트 강의', '테스트 강의 설명', 20000, 0, 0, '테스트 대상자', null, true, NOW(), NOW(), false);",
            "INSERT INTO web_service.community_questions (id, user_id, course_id, title, content, status, created_at, updated_at, is_deleted) "
                    + "VALUES (1, 1, 1, '질문A', '질문A 내용', 'ACCEPTED', NOW(), NOW(), false);",
            "INSERT INTO web_service.community_questions (id, user_id, course_id, title, content, status, created_at, updated_at, is_deleted) "
                    + "VALUES (2, 1, 1, '질문B', '질문B 내용', 'WAITING', NOW(), NOW(), false);",
            "INSERT INTO web_service.community_answers (id, user_id, question_id, content, is_accepted, created_at, updated_at, is_deleted) "
                    + "VALUES (1, 2, 1, '질문A의 채택된 답변', true, DATEADD('MINUTE', -30, NOW()), NOW(), false);",
            "INSERT INTO web_service.community_answers (id, user_id, question_id, content, is_accepted, created_at, updated_at, is_deleted) "
                    + "VALUES (2, 1, 1, '질문A의 일반 답변', false, DATEADD('MINUTE', -20, NOW()), NOW(), false);",
            "INSERT INTO web_service.community_answers (id, user_id, question_id, content, is_accepted, created_at, updated_at, is_deleted) "
                    + "VALUES (3, 2, 2, '질문B의 답변', false, NOW(), NOW(), false);",
            "INSERT INTO web_service.files (container_id, container_type, content_type, name, original_name, size, created_at, updated_at) "
                    + "VALUES (1, 'ANSWER_IMAGE', 'image/jpeg', 'accepted_attachment.jpg', 'accepted_attachment.jpg', 1024, NOW(), NOW());",
            "INSERT INTO web_service.community_answers_files (answer_id, file_id, created_at, updated_at) "
                    + "VALUES (1, (SELECT id FROM web_service.files WHERE name = 'accepted_attachment.jpg'), NOW(), NOW());",
            "INSERT INTO web_service.video_answers (id, video_uuid, community_answer_id, user_id, s3key, extension, original_file_name, duration, encoding_status, encoding_at, created_at, updated_at, is_deleted) "
                    + "VALUES (1, '00000000-0000-0000-0000-000000000001', 1, 2, 'vod/ANSWER/mp4/00000000-0000-0000-0000-000000000001/accepted_answer_video.mp4', 'mp4', 'accepted_answer_video.mp4', 20, 'COMPLETED', NOW(), NOW(), NOW(), false);"})
    @Test
    void getAcceptedAnswers_정상() {
        // given
        Long questionId = 1L;

        // when
        List<CourseAnswerRes> result = courseAnswerService.getAcceptedAnswers(questionId);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);

        CourseAnswerRes acceptedAnswer = result.get(0);
        assertThat(acceptedAnswer.answerId()).isEqualTo(1L);
        assertThat(acceptedAnswer.content()).isEqualTo("질문A의 채택된 답변");
        assertThat(acceptedAnswer.isAccepted()).isTrue();

        // 사용자 정보 검증
        assertThat(acceptedAnswer.user().id()).isEqualTo(2L);
        assertThat(acceptedAnswer.user().nickname()).isEqualTo("사용자2");
        assertThat(acceptedAnswer.user().userType()).isEqualTo(UserType.LEARNER);

        // 첨부파일 검증
        assertThat(acceptedAnswer.attachments()).hasSize(1);
        assertThat(acceptedAnswer.attachments().get(0).name()).isEqualTo("accepted_attachment.jpg");
        assertThat(acceptedAnswer.attachments().get(0).contentType()).isEqualTo("image/jpeg");

        // 비디오 정보 검증
        assertThat(acceptedAnswer.videoInfo()).isNotNull();
        assertThat(acceptedAnswer.videoInfo().videoUuid()).isEqualTo(
                UUID.fromString("00000000-0000-0000-0000-000000000001"));
        assertThat(acceptedAnswer.videoInfo().originFileName()).isEqualTo("accepted_answer_video.mp4");
    }

}
