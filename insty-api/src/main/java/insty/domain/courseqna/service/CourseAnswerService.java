package insty.domain.courseqna.service;

import insty.domain.common.FileInfo;
import insty.domain.common.SearchRes;
import insty.domain.common.dto.PaginationRes;
import insty.domain.courseqna.dto.CourseQnaAcceptAnswerResultRes;
import insty.domain.courseqna.dto.CourseAnswerCreateReq;
import insty.domain.courseqna.dto.CourseAnswerRes;
import insty.domain.courseqna.dto.CourseAnswerSearchReq;
import insty.domain.courseqna.dto.CourseAnswerUpdateReq;
import insty.domain.courseqna.implement.CourseAnswerAcceptManager;
import insty.domain.courseqna.implement.CourseAnswerFileReader;
import insty.domain.courseqna.implement.CourseAnswerFileWriter;
import insty.domain.courseqna.implement.CourseAnswerMapper;
import insty.domain.courseqna.implement.CourseAnswerReader;
import insty.domain.courseqna.implement.CourseAnswerVideoManager;
import insty.domain.courseqna.implement.CourseAnswerWriter;
import insty.domain.courseqna.implement.CourseMentionManager;
import insty.domain.courseqna.implement.CourseNotificationManager;
import insty.domain.courseqna.implement.CourseQuestionReader;
import insty.domain.courseqna.implement.CourseQuestionStatusManager;
import insty.domain.courseqna.implement.CourseQnaValidator;
import insty.domain.courseqna.repository.CourseQuestionRepository;
import insty.domain.user.implement.UserReader;
import insty.model.courseqna.CourseAnswer;
import insty.model.courseqna.CourseQuestion;
import insty.model.user.User;
import insty.model.video.VideoAnswer;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
public class CourseAnswerService {
    private final CourseAnswerReader courseAnswerReader;
    private final CourseAnswerWriter courseAnswerWriter;
    private final CourseAnswerFileReader courseAnswerFileReader;
    private final CourseAnswerFileWriter courseAnswerFileWriter;
    private final CourseAnswerVideoManager courseAnswerVideoManager;
    private final CourseAnswerAcceptManager courseAnswerAcceptManager;
    private final CourseQnaValidator courseQnaValidator;
    private final CourseAnswerMapper courseAnswerMapper;
    private final CourseQuestionReader courseQuestionReader;
    private final CourseQuestionStatusManager courseQuestionStatusManager;
    private final UserReader userReader;
    private final CourseNotificationManager courseNotificationManager;
    private final CourseMentionManager courseMentionManager;
    private final CourseQuestionRepository courseQuestionRepository;

    /**
     * 새로운 답변을 생성하고 이미지 파일과 비디오 파일을 저장
     */
    public CourseAnswerRes saveAnswer(Long userId, Long questionId, CourseAnswerCreateReq req, List<MultipartFile> attachments) {
        courseQnaValidator.validateContent(req.content());
        courseQnaValidator.validateFiles(attachments);

        // 답변 이미지 파일은 한 개로 보장
        courseQnaValidator.validateAnswerFileCount(attachments);

        CourseQuestion question = courseQuestionReader.getCommunityQuestionWithFilesById(questionId);
        User user = userReader.getUser(userId);

        CourseAnswer answer = courseAnswerWriter.saveAnswer(user, question, req);
        List<FileInfo> fileInfos = courseAnswerFileWriter.saveAnswerFiles(answer, attachments);
        VideoAnswer video = courseAnswerVideoManager.attachVideoToAnswer(answer, req.videoUuid());

        courseQuestionStatusManager.updateStatusAfterAnswerCreated(question);

        List<User> mentionedUsers = courseMentionManager.processMentions(answer, user, answer.getContent());

        courseNotificationManager.sendNewAnswerNotification(question, answer, mentionedUsers);

        return CourseAnswerRes.from(answer, fileInfos, video);
    }

    /**
     * 기존 답변을 수정하고 첨부 파일을 업데이트
     */
    public CourseAnswerRes updateAnswer(Long userId, Long answerId, CourseAnswerUpdateReq req, List<MultipartFile> attachments) {
        courseQnaValidator.validateContent(req.content());
        courseQnaValidator.validateFiles(attachments);

        CourseAnswer current = courseAnswerReader.getCommunityAnswerById(answerId);
        courseQnaValidator.validateAnswerAuthor(userId, current);
        courseQnaValidator.validateAnswerFileCountForUpdate(current, attachments, req.deleteFileIds());

        CourseAnswer updatedAnswer = courseAnswerWriter.updateAnswer(answerId, req);

        List<FileInfo> fileInfos = courseAnswerFileWriter.updateAnswerFiles(updatedAnswer, attachments, req.deleteFileIds());
        VideoAnswer video = courseAnswerVideoManager.updateAndGetLinkedVideo(updatedAnswer, req.videoUuid());

        return CourseAnswerRes.from(updatedAnswer, fileInfos, video);
    }

    /**
     * 특정 질문에 달린 모든 답변을 상세 정보와 함께 조회
     */
    public List<CourseAnswerRes> getAllAnswersByQuestionId(Long questionId) {
        courseQnaValidator.validateQuestionExists(questionId);
        List<CourseAnswer> answers = courseAnswerReader.getAllCommunityAnswersByQuestionId(questionId);
        var videoMap = courseAnswerVideoManager.getVideoMapByAnswers(answers);
        return courseAnswerMapper.toCourseAnswerResList(answers, videoMap);
    }

    /**
     * 특정 질문에 달린 답변을 페이지네이션으로 조회
     */
    @Transactional(readOnly = true)
    public SearchRes<CourseAnswerRes> getAnswersByQuestionId(Long questionId, CourseAnswerSearchReq req) {
        courseQnaValidator.validateQuestionExists(questionId);
        
        Page<CourseAnswer> answersPage = courseAnswerReader.getCommunityAnswersByQuestionIdWithPagination(questionId, req.toPaginationReq());
        
        var videoMap = courseAnswerVideoManager.getVideoMapByAnswers(answersPage.getContent());
        List<CourseAnswerRes> answerResList = courseAnswerMapper.toCourseAnswerResList(answersPage.getContent(), videoMap);
        
        final int totalItems = Math.toIntExact(answersPage.getTotalElements());
        PaginationRes paginationRes = PaginationRes.of(
                totalItems,
                req.page(),
                req.pageSize()
        );
        
        return SearchRes.from(paginationRes, answerResList);
    }


    /**
     * 답변의 모든 정보와 첨부 파일을 포함하여 조회
     */
    public CourseAnswerRes getAnswerDetails(Long answerId) {
        CourseAnswer answer = courseAnswerReader.getCommunityAnswerById(answerId);
        List<FileInfo> fileInfos = courseAnswerFileReader.getAnswerFileInfos(answer);
        VideoAnswer video = courseAnswerVideoManager.getVideoAnswer(answer);
        return CourseAnswerRes.from(answer, fileInfos, video);
    }

    /**
     * 답변과 관련된 모든 데이터(첨부 파일 등)를 함께 삭제
     */
    public void deleteAnswer(Long userId, Long answerId) {
        CourseAnswer answer = courseAnswerReader.getCommunityAnswerById(answerId);
        courseQnaValidator.validateAnswerAuthor(userId, answer);
        
        // 채택된 답변 삭제 시 먼저 채택 상태 해제
        CourseQuestion question = answer.getCourseQuestion();
        boolean wasAccepted = answer.isAccepted();
        if (wasAccepted) {
            question.handleAcceptedAnswerDeleted(true); // 임시로 true, 나중에 정확한 값으로 재계산
        }
        
        courseAnswerFileWriter.deleteAnswerFiles(answer);
        courseAnswerVideoManager.deleteAnswerVideo(answer);
        courseAnswerWriter.deleteAnswer(answer);

        // 채택되지 않은 답변이거나 이미 처리된 경우만 상태 업데이트
        if (!wasAccepted) {
            courseQuestionStatusManager.updateStatusAfterAnswerDeleted(answer);
        } else {
            // 채택된 답변 삭제 후 정확한 상태로 재설정
            int remainingAnswers = courseAnswerReader.countActiveAnswersByQuestionId(question.getId());
            question.changeStatusByAnswer(remainingAnswers > 0);
            courseQuestionRepository.save(question);
        }
    }

    /**
     * 질문 작성자가 특정 답변을 채택
     * -> 한 질문에는 하나의 답변만 채택할 수 있습니다.
     */
    public CourseQnaAcceptAnswerResultRes acceptAnswer(Long userId, Long questionId, Long answerId) {
        CourseQuestion question = courseQuestionReader.getCommunityQuestionWithFilesById(questionId);
        CourseAnswer answer = courseAnswerReader.getCommunityAnswerById(answerId);
        courseQnaValidator.validateQuestionAuthor(userId, questionId);
        courseQnaValidator.validateAnswerBelongsToQuestion(answer, question);

        CourseQnaAcceptAnswerResultRes result = courseAnswerAcceptManager.acceptAnswer(question, answer);

        if (result.accepted()) {
            courseNotificationManager.sendAnswerAcceptedNotification(question, answer);
        }
        
        return result;
    }

    /**
     * 특정 질문의 채택된 답변을 조회
     */
    @Transactional(readOnly = true)
    public List<CourseAnswerRes> getAcceptedAnswers(Long questionId) {
        courseQnaValidator.validateQuestionExists(questionId);
        
        List<CourseAnswer> acceptedAnswers = courseAnswerReader.getAcceptedAnswersByQuestionId(questionId);
        var videoMap = courseAnswerVideoManager.getVideoMapByAnswers(acceptedAnswers);
        
        return courseAnswerMapper.toCourseAnswerResList(acceptedAnswers, videoMap);
    }
}