package insty.domain.community.service;

import insty.domain.common.FileInfo;
import insty.domain.common.VideoInfo;
import insty.domain.community.dto.AcceptAnswerResultRes;
import insty.domain.community.dto.CommunityAnswerCreateReq;
import insty.domain.community.dto.CommunityAnswerRes;
import insty.domain.community.dto.CommunityAnswerUpdateReq;
import insty.domain.community.implement.CommunityAnswerAcceptService;
import insty.domain.community.implement.CommunityAnswerFileReader;
import insty.domain.community.implement.CommunityAnswerFileWriter;
import insty.domain.community.implement.CommunityAnswerReader;
import insty.domain.community.implement.CommunityAnswerVideoManager;
import insty.domain.community.implement.CommunityAnswerWriter;
import insty.domain.community.implement.CommunityQuestionReader;
import insty.domain.community.implement.CommunityValidator;
import insty.domain.user.implement.UserReader;
import insty.model.community.CommunityAnswer;
import insty.model.community.CommunityQuestion;
import insty.model.user.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
    private final CommunityQuestionReader communityQuestionReader;
    private final UserReader userReader;

    /**
     * 특정 질문에 달린 모든 답변을 상세 정보와 함께 조회
     */
    public List<CommunityAnswerRes> getAllAnswersByQuestionId(Long questionId) {
        communityValidator.validateQuestionExists(questionId);
        List<CommunityAnswer> answers = communityAnswerReader.getAllCommunityAnswersByQuestionId(questionId);
        List<CommunityAnswerRes> answerRes = answers.stream()
                .map(answer -> CommunityAnswerRes.from(
                        answer,
                        communityAnswerFileReader.getAnswerFileInfos(answer),
                        communityAnswerVideoManager.getAnswerVideoInfo(answer)
                ))
                .toList();

        return answerRes;
    }


    /**
     * 답변의 모든 정보와 첨부 파일을 포함하여 조회
     */
    public CommunityAnswerRes getAnswerDetails(Long answerId) {
        CommunityAnswer answer = communityAnswerReader.getCommunityAnswerById(answerId);

        List<FileInfo> fileInfos = communityAnswerFileReader.getAnswerFileInfos(answer);
        VideoInfo videoInfo = communityAnswerVideoManager.getAnswerVideoInfo(answer);

        return CommunityAnswerRes.from(answer, fileInfos, videoInfo);
    }

    /**
     * 새로운 답변을 생성하고 이미지 파일과 비디오 파일을 저장
     */
    public CommunityAnswerRes saveAnswer(Long userId, CommunityAnswerCreateReq req, List<MultipartFile> attachments) {
        communityValidator.validateAnswerCreateRequest(req);
        communityValidator.validateFiles(attachments);

        CommunityQuestion question = communityQuestionReader.getCommunityQuestionDetailsById(req.questionId());
        User user = userReader.getUser(userId);

        CommunityAnswer answer = communityAnswerWriter.saveAnswer(user, question, req);
        List<FileInfo> fileInfos = communityAnswerFileWriter.saveAnswerFiles(answer, attachments);
        VideoInfo videoInfo = communityAnswerVideoManager.saveAnswerVideo(answer, req.videoUuid());

        return CommunityAnswerRes.from(answer, fileInfos, videoInfo);
    }

    /**
     * 기존 답변을 수정하고 첨부 파일을 업데이트
     */
    public CommunityAnswerRes updateAnswer(Long userId, Long answerId, CommunityAnswerUpdateReq req, List<MultipartFile> attachments) {
        communityValidator.validateAnswerUpdateRequest(req);
        communityValidator.validateFiles(attachments);
        communityValidator.validateAnswerAuthor(userId, answerId);

        CommunityAnswer answer = communityAnswerWriter.updateAnswer(answerId, req);
        List<FileInfo> fileInfos = communityAnswerFileWriter.updateAnswerFiles(answer, attachments, req.deleteFileIds());
        VideoInfo videoInfo = communityAnswerVideoManager.saveAnswerVideo(answer, req.videoUuid());

        return CommunityAnswerRes.from(answer, fileInfos, videoInfo);
    }

    /**
     * 답변과 관련된 모든 데이터(첨부 파일 등)를 함께 삭제
     */
    public void deleteAnswer(Long userId, Long answerId) {
        communityValidator.validateAnswerAuthor(userId, answerId);
        CommunityAnswer answer = communityAnswerReader.getCommunityAnswerById(answerId);
        communityAnswerWriter.deleteAnswer(answer);
    }

    /**
     * 질문 작성자가 특정 답변을 채택
     * -> 한 질문에는 하나의 답변만 채택할 수 있습니다.
     */
    public AcceptAnswerResultRes acceptAnswer(Long userId, Long questionId, Long answerId) {
        communityValidator.validateAnswerAuthor(userId, answerId);
        CommunityQuestion question = communityQuestionReader.getCommunityQuestionDetailsById(questionId);
        CommunityAnswer answer = communityAnswerReader.getCommunityAnswerById(answerId);
        communityValidator.validateAnswerBelongsToQuestion(answer, question);
        return communityAnswerAcceptService.acceptAnswer(question, answer);
    }
}