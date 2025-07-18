package insty.domain.community.implement;

import insty.domain.community.repository.CommunityAnswerRepository;
import insty.domain.community.repository.CommunityQuestionRepository;
import insty.domain.community.repository.CommunityAnswerFileRepository;
import insty.error.CommunityErrorCode;
import insty.exception.CustomException;
import insty.model.community.CommunityAnswer;
import insty.model.community.CommunityAnswerFile;
import insty.model.community.CommunityQuestion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CommunityReader {

    private final CommunityQuestionRepository communityQuestionRepository;
    private final CommunityAnswerRepository communityAnswerRepository;
    private final CommunityAnswerFileRepository communityAnswerFileRepository;

    public List<CommunityQuestion> getAllCommunityQuestions() {
        return communityQuestionRepository.findAll();
    }

    /**
     * 특정 강좌의 모든 커뮤니티 질문 조회
     */
    public List<CommunityQuestion> getAllCommunityQuestionsByCourseId(Long courseId) {
        return communityQuestionRepository.findAllByCourseId(courseId);
    }

    /**
     * 커뮤니티 질문 상세조회
     */
    public CommunityQuestion getCommunityQuestionDetailsById(Long questionId) {
        return  communityQuestionRepository.findById(questionId)
                .orElseThrow(() -> new CustomException(CommunityErrorCode.COMMUNITY_QUESTION_NOT_FOUND));
    }

    /**
     * 커뮤니티 답변 조회
     */
    public CommunityAnswer getCommunityAnswerById(Long answerId) {
        return communityAnswerRepository.findById(answerId)
                .orElseThrow(() -> new CustomException(CommunityErrorCode.COMMUNITY_ANSWER_NOT_FOUND));
    }

    /**
     * 커뮤니티 답변에 따른 모든 질문 조회
     */
    public List<CommunityAnswer> getAllCommunityAnswers(Long questionId) {
        return communityAnswerRepository.findAllByCommunityQuestionId(questionId);
    }

    /**
     * 커뮤니티 질문에 모든 파일 조회
     */
    public List<CommunityAnswerFile> getCommunityAnswerFilesByAnswerId(Long answerId) {
        return communityAnswerFileRepository.findAllByCommunityAnswerId(answerId);
    }
}
