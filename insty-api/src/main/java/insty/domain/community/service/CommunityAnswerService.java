package insty.domain.community.service;

import insty.domain.common.FileInfo;
import insty.domain.common.SearchRes;
import insty.domain.common.dto.PaginationRes;
import insty.domain.community.dto.AcceptAnswerResultRes;
import insty.domain.community.dto.CommunityAnswerCreateReq;
import insty.domain.community.dto.CommunityAnswerRes;
import insty.domain.community.dto.CommunityAnswerSearchReq;
import insty.domain.community.dto.CommunityAnswerUpdateReq;
import insty.domain.community.implement.CommunityAnswerAcceptManager;
import insty.domain.community.implement.CommunityAnswerFileReader;
import insty.domain.community.implement.CommunityAnswerFileWriter;
import insty.domain.community.implement.CommunityAnswerMapper;
import insty.domain.community.implement.CommunityAnswerReader;
import insty.domain.community.implement.CommunityAnswerVideoManager;
import insty.domain.community.implement.CommunityAnswerWriter;
import insty.domain.community.implement.CommunityMentionManager;
import insty.domain.community.implement.CommunityNotificationManager;
import insty.domain.community.implement.CommunityQuestionReader;
import insty.domain.community.implement.CommunityQuestionStatusManager;
import insty.domain.community.implement.CommunityValidator;
import insty.domain.community.repository.CommunityQuestionRepository;
import insty.domain.user.implement.UserReader;
import insty.model.community.CommunityAnswer;
import insty.model.community.CommunityQuestion;
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
public class CommunityAnswerService {
    private final CommunityAnswerReader communityAnswerReader;
    private final CommunityAnswerWriter communityAnswerWriter;
    private final CommunityAnswerFileReader communityAnswerFileReader;
    private final CommunityAnswerFileWriter communityAnswerFileWriter;
    private final CommunityAnswerVideoManager communityAnswerVideoManager;
    private final CommunityAnswerAcceptManager communityAnswerAcceptManager;
    private final CommunityValidator communityValidator;
    private final CommunityAnswerMapper communityAnswerMapper;
    private final CommunityQuestionReader communityQuestionReader;
    private final CommunityQuestionStatusManager communityQuestionStatusManager;
    private final UserReader userReader;
    private final CommunityNotificationManager communityNotificationManager;
    private final CommunityMentionManager communityMentionManager;
    private final CommunityQuestionRepository communityQuestionRepository;

    /**
     * 새로운 답변을 생성하고 이미지 파일과 비디오 파일을 저장
     */
    public CommunityAnswerRes saveAnswer(Long userId, Long questionId, CommunityAnswerCreateReq req, List<MultipartFile> attachments) {
        communityValidator.validateContent(req.content());
        communityValidator.validateFiles(attachments);

        CommunityQuestion question = communityQuestionReader.getCommunityQuestionWithFilesById(questionId);
        User user = userReader.getUser(userId);

        CommunityAnswer answer = communityAnswerWriter.saveAnswer(user, question, req);
        List<FileInfo> fileInfos = communityAnswerFileWriter.saveAnswerFiles(answer, attachments);
        VideoAnswer video = communityAnswerVideoManager.attachVideoToAnswer(answer, req.videoUuid());

        communityQuestionStatusManager.updateStatusAfterAnswerCreated(question);

        List<User> mentionedUsers = communityMentionManager.processMentions(answer, user, answer.getContent());

        communityNotificationManager.sendNewAnswerNotification(question, answer, mentionedUsers);

        return CommunityAnswerRes.from(answer, fileInfos, video);
    }

    /**
     * 기존 답변을 수정하고 첨부 파일을 업데이트
     */
    public CommunityAnswerRes updateAnswer(Long userId, Long answerId, CommunityAnswerUpdateReq req, List<MultipartFile> attachments) {
        communityValidator.validateContent(req.content());
        communityValidator.validateFiles(attachments);

        CommunityAnswer answer = communityAnswerWriter.updateAnswer(answerId, req);
        communityValidator.validateAnswerAuthor(userId, answer);

        List<FileInfo> fileInfos = communityAnswerFileWriter.updateAnswerFiles(answer, attachments, req.deleteFileIds());
        VideoAnswer video = communityAnswerVideoManager.updateAndGetLinkedVideo(answer, req.videoUuid());

        return CommunityAnswerRes.from(answer, fileInfos, video);
    }

    /**
     * 특정 질문에 달린 모든 답변을 상세 정보와 함께 조회
     */
    public List<CommunityAnswerRes> getAllAnswersByQuestionId(Long questionId) {
        communityValidator.validateQuestionExists(questionId);
        List<CommunityAnswer> answers = communityAnswerReader.getAllCommunityAnswersByQuestionId(questionId);
        var videoMap = communityAnswerVideoManager.getVideoMapByAnswers(answers);
        return communityAnswerMapper.toCommunityAnswerResList(answers, videoMap);
    }

    /**
     * 특정 질문에 달린 답변을 페이지네이션으로 조회
     */
    @Transactional(readOnly = true)
    public SearchRes<CommunityAnswerRes> getAnswersByQuestionId(Long questionId, CommunityAnswerSearchReq req) {
        communityValidator.validateQuestionExists(questionId);
        
        Page<CommunityAnswer> answersPage = communityAnswerReader.getCommunityAnswersByQuestionIdWithPagination(questionId, req.toPaginationReq());
        
        var videoMap = communityAnswerVideoManager.getVideoMapByAnswers(answersPage.getContent());
        List<CommunityAnswerRes> answerResList = communityAnswerMapper.toCommunityAnswerResList(answersPage.getContent(), videoMap);
        
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
    public CommunityAnswerRes getAnswerDetails(Long answerId) {
        CommunityAnswer answer = communityAnswerReader.getCommunityAnswerById(answerId);
        List<FileInfo> fileInfos = communityAnswerFileReader.getAnswerFileInfos(answer);
        VideoAnswer video = communityAnswerVideoManager.getVideoAnswer(answer);
        return CommunityAnswerRes.from(answer, fileInfos, video);
    }

    /**
     * 답변과 관련된 모든 데이터(첨부 파일 등)를 함께 삭제
     */
    public void deleteAnswer(Long userId, Long answerId) {
        CommunityAnswer answer = communityAnswerReader.getCommunityAnswerById(answerId);
        communityValidator.validateAnswerAuthor(userId, answer);
        
        // 채택된 답변 삭제 시 먼저 채택 상태 해제
        CommunityQuestion question = answer.getCommunityQuestion();
        boolean wasAccepted = answer.isAccepted();
        if (wasAccepted) {
            question.handleAcceptedAnswerDeleted(true); // 임시로 true, 나중에 정확한 값으로 재계산
        }
        
        communityAnswerFileWriter.deleteAnswerFiles(answer);
        communityAnswerVideoManager.deleteAnswerVideo(answer);
        communityAnswerWriter.deleteAnswer(answer);

        // 채택되지 않은 답변이거나 이미 처리된 경우만 상태 업데이트
        if (!wasAccepted) {
            communityQuestionStatusManager.updateStatusAfterAnswerDeleted(answer);
        } else {
            // 채택된 답변 삭제 후 정확한 상태로 재설정
            int remainingAnswers = communityAnswerReader.countActiveAnswersByQuestionId(question.getId());
            question.changeStatusByAnswer(remainingAnswers > 0);
            communityQuestionRepository.save(question);
        }
    }

    /**
     * 질문 작성자가 특정 답변을 채택
     * -> 한 질문에는 하나의 답변만 채택할 수 있습니다.
     */
    public AcceptAnswerResultRes acceptAnswer(Long userId, Long questionId, Long answerId) {
        CommunityQuestion question = communityQuestionReader.getCommunityQuestionWithFilesById(questionId);
        CommunityAnswer answer = communityAnswerReader.getCommunityAnswerById(answerId);
        communityValidator.validateQuestionAuthor(userId, questionId);
        communityValidator.validateAnswerBelongsToQuestion(answer, question);

        AcceptAnswerResultRes result = communityAnswerAcceptManager.acceptAnswer(question, answer);

        if (result.accepted()) {
            communityNotificationManager.sendAnswerAcceptedNotification(question, answer);
        }
        
        return result;
    }

    /**
     * 특정 질문의 채택된 답변을 조회
     */
    @Transactional(readOnly = true)
    public List<CommunityAnswerRes> getAcceptedAnswers(Long questionId) {
        communityValidator.validateQuestionExists(questionId);
        
        List<CommunityAnswer> acceptedAnswers = communityAnswerReader.getAcceptedAnswersByQuestionId(questionId);
        var videoMap = communityAnswerVideoManager.getVideoMapByAnswers(acceptedAnswers);
        
        return communityAnswerMapper.toCommunityAnswerResList(acceptedAnswers, videoMap);
    }
}