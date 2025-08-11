package insty.domain.community.service;

import insty.domain.common.FileInfo;
import insty.domain.community.dto.AcceptAnswerResultRes;
import insty.domain.community.dto.CommunityAnswerCreateReq;
import insty.domain.community.dto.CommunityAnswerRes;
import insty.domain.community.dto.CommunityAnswerUpdateReq;
import insty.domain.community.event.CommunityAnswerCreatedEvent;
import insty.domain.community.implement.CommunityAnswerAcceptService;
import insty.domain.community.implement.CommunityAnswerFileReader;
import insty.domain.community.implement.CommunityAnswerFileWriter;
import insty.domain.community.implement.CommunityAnswerMapper;
import insty.domain.community.implement.CommunityAnswerReader;
import insty.domain.community.implement.CommunityAnswerVideoManager;
import insty.domain.community.implement.CommunityAnswerWriter;
import insty.domain.community.implement.CommunityQuestionReader;
import insty.domain.community.implement.CommunityValidator;
import insty.domain.user.implement.UserReader;
import insty.model.community.CommunityAnswer;
import insty.model.community.CommunityQuestion;
import insty.model.user.User;
import insty.model.video.VideoAnswer;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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
    private final CommunityAnswerAcceptService communityAnswerAcceptService;
    private final CommunityValidator communityValidator;
    private final CommunityAnswerMapper communityAnswerMapper;
    private final CommunityQuestionReader communityQuestionReader;
    private final UserReader userReader;
    private final ApplicationEventPublisher eventPublisher;

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

        eventPublisher.publishEvent(new CommunityAnswerCreatedEvent(question.getId(), answer.getId()));

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
        communityAnswerFileWriter.deleteAnswerFiles(answer);
        communityAnswerVideoManager.deleteeAnswerVideo(answer);
        communityAnswerWriter.deleteAnswer(answer);
    }

    /**
     * 질문 작성자가 특정 답변을 채택
     * -> 한 질문에는 하나의 답변만 채택할 수 있습니다.
     */
    public AcceptAnswerResultRes acceptAnswer(Long userId, Long questionId, Long answerId) {
        CommunityQuestion question = communityQuestionReader.getCommunityQuestionWithFilesById(questionId);
        CommunityAnswer answer = communityAnswerReader.getCommunityAnswerById(answerId);
        communityValidator.validateAnswerAuthor(userId, answer);
        communityValidator.validateAnswerBelongsToQuestion(answer, question);
        return communityAnswerAcceptService.acceptAnswer(question, answer);
    }
}