package insty.domain.community.implement;


import insty.domain.community.repository.CommunityAnswerFileRepository;
import insty.domain.community.repository.CommunityAnswerRepository;
import insty.error.CommunityErrorCode;
import insty.exception.CustomException;
import insty.model.community.CommunityAnswer;
import insty.model.community.CommunityAnswerFile;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CommunityAnswerReader {

    private final CommunityAnswerRepository communityAnswerRepository;
    private final CommunityAnswerFileRepository communityAnswerFileRepository;

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