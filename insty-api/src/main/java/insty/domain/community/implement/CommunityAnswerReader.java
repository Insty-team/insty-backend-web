package insty.domain.community.implement;


import insty.domain.community.repository.CommunityAnswerRepository;
import insty.error.CommunityErrorCode;
import insty.exception.CustomException;
import insty.model.community.CommunityAnswer;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CommunityAnswerReader {
    private final CommunityAnswerRepository communityAnswerRepository;

    /**
     * 커뮤니티 답변에 따른 모든 질문 조회
     */
    public List<CommunityAnswer> getAllCommunityAnswersByQuestionId(Long questionId) {
        return communityAnswerRepository.findAllDetailsWithUserAttachmentsByCommunityQuestionId(questionId);
    }

    /**
     * 커뮤니티 답변 상세 조회
     */
    public CommunityAnswer getCommunityAnswerById(Long answerId) {
        CommunityAnswer answer = communityAnswerRepository.findById(answerId)
                .orElseThrow(() -> new CustomException(CommunityErrorCode.COMMUNITY_ANSWER_NOT_FOUND));

        if (answer.isDeleted()) {
            throw new CustomException(CommunityErrorCode.COMMUNITY_ANSWER_ALREADY_DELETED);
        }

        return answer;
    }

    /**
     * 삭제된 답변도 포함하여 커뮤니티 답변 조회
     */
    public CommunityAnswer getCommunityAnswerByIdIncludingDeleted(Long answerId) {
        return communityAnswerRepository.findById(answerId)
                .orElseThrow(() -> new CustomException(CommunityErrorCode.COMMUNITY_ANSWER_NOT_FOUND));
    }
}