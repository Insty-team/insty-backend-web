package insty.domain.community.implement;

import insty.domain.community.dto.CommunityAnswerCreateReq;
import insty.domain.community.dto.CommunityAnswerUpdateReq;
import insty.domain.community.dto.CommunityQuestionCreateReq;
import insty.domain.community.dto.CommunityQuestionUpdateReq;
import insty.domain.community.repository.CommunityAnswerFileRepository;
import insty.domain.community.repository.CommunityAnswerRepository;
import insty.domain.community.repository.CommunityFileRepository;
import insty.domain.community.repository.CommunityQuestionRepository;
import insty.error.CommunityErrorCode;
import insty.exception.CustomException;
import insty.model.community.CommunityAnswer;
import insty.model.community.CommunityAnswerFile;
import insty.model.community.CommunityFile;
import insty.model.community.CommunityQuestion;
import insty.model.course.Course;
import insty.model.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CommunityWriter {

    private final CommunityQuestionRepository communityQuestionRepository;
    private final CommunityAnswerRepository communityAnswerRepository;
    private final CommunityFileRepository communityFileRepository;
    private final CommunityAnswerFileRepository communityAnswerFileRepository;

    // ===================== 질문 관련 =====================

    /**
     * 커뮤니티 질문 생성 및 저장
     */
    public CommunityQuestion saveQuestion(User user, Course course, CommunityQuestionCreateReq req) {
        CommunityQuestion question = CommunityQuestion.create(course, user, req.title(), req.content());
        return communityQuestionRepository.save(question);
    }

    /**
     * 커뮤니티 질문 수정 (id로 직접 조회)
     */
    public CommunityQuestion updateQuestion(Long questionId, CommunityQuestionUpdateReq req) {
        CommunityQuestion question = communityQuestionRepository.findById(questionId)
                .orElseThrow(() -> new CustomException(CommunityErrorCode.COMMUNITY_QUESTION_NOT_FOUND));
        question.update(req.title(), req.content(), question.getAttachments());
        return communityQuestionRepository.save(question);
    }

    /**
     * 커뮤니티 질문 삭제
     */
    public void deleteQuestion(CommunityQuestion communityQuestion) {
        communityQuestionRepository.delete(communityQuestion);
    }

    /**
     * 커뮤니티 파일 목록 저장
     */
    public List<CommunityFile> saveCommunityFiles(List<CommunityFile> communityFiles) {
        return communityFileRepository.saveAll(communityFiles);
    }

    /**
     * 커뮤니티 파일 목록 삭제
     */
    public void deleteCommunityFiles(List<CommunityFile> communityFiles) {
        communityFileRepository.deleteAll(communityFiles);
    }

    // ===================== 답변 관련 =====================

    /**
     * 커뮤니티 답변 생성 및 저장
     */
    public CommunityAnswer saveAnswer(User user, CommunityQuestion question, CommunityAnswerCreateReq req) {
        CommunityAnswer answer = CommunityAnswer.create(question, user, req.content());
        return communityAnswerRepository.save(answer);
    }

    /**
     * 커뮤니티 답변 수정 (id로 직접 조회)
     */
    public CommunityAnswer updateAnswer(Long answerId, CommunityAnswerUpdateReq req) {
        CommunityAnswer answer = communityAnswerRepository.findById(answerId)
                .orElseThrow(() -> new CustomException(CommunityErrorCode.COMMUNITY_ANSWER_NOT_FOUND));
        answer.update(req.content());
        return communityAnswerRepository.save(answer);
    }

    /**
     * 커뮤니티 답변 삭제
     */
    public void deleteAnswer(CommunityAnswer communityAnswer) {
        communityAnswerRepository.delete(communityAnswer);
    }

    /**
     * 커뮤니티 답변 파일 목록 저장
     */
    public List<CommunityAnswerFile> saveCommunityAnswerFiles(List<CommunityAnswerFile> communityAnswerFiles) {
        return communityAnswerFileRepository.saveAll(communityAnswerFiles);
    }

    /**
     * 커뮤니티 답변 파일 저장
     */
    public CommunityAnswerFile saveCommunityAnswerFile(CommunityAnswerFile communityAnswerFile) {
        return communityAnswerFileRepository.save(communityAnswerFile);
    }

    /**
     * 커뮤니티 답변 파일 목록 삭제
     */
    public void deleteCommunityAnswerFiles(List<CommunityAnswerFile> communityAnswerFiles) {
        communityAnswerFileRepository.deleteAll(communityAnswerFiles);
    }

    /**
     * 커뮤니티 답변 채택 처리
     */
    public void acceptAnswer(CommunityQuestion communityQuestion, CommunityAnswer communityAnswer) {
        communityQuestion.acceptAnswer(communityAnswer);
        communityQuestionRepository.save(communityQuestion);
    }

    /**
     * 커뮤니티 답변 채택 해제
     */
    public void unacceptAnswer(CommunityQuestion communityQuestion) {
        communityQuestion.unacceptAnswer();
        communityQuestionRepository.save(communityQuestion);
    }
}
