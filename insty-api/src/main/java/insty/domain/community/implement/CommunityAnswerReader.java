package insty.domain.community.implement;


import insty.domain.community.repository.CommunityAnswerRepository;
import insty.error.CommunityErrorCode;
import insty.exception.CustomException;
import insty.model.community.CommunityAnswer;
import insty.model.user.User;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CommunityAnswerReader {
    private final CommunityAnswerRepository communityAnswerRepository;

    /**
     * 특정 질문의 모든 답변 조회
     */
    public List<CommunityAnswer> getAllCommunityAnswersByQuestionId(Long questionId) {
        return communityAnswerRepository.findAllDetailsWithUserAttachmentsByCommunityQuestionId(questionId);
    }

    /**
     * 커뮤니티 답변을 페이지네이션으로 조회
     */
    public Page<CommunityAnswer> getCommunityAnswersByQuestionIdWithPagination(Long questionId, Pageable pageable) {
        return communityAnswerRepository.findAllDetailsWithUserAttachmentsByCommunityQuestionIdWithPagination(questionId, pageable);
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

    /**
     * 특정 질문에 대한 활성 답변 개수 조회
     */
    public int countActiveAnswersByQuestionId(Long questionId) {
        return communityAnswerRepository.countByCommunityQuestionIdAndIsDeletedFalse(questionId);
    }

    /**
     * 특정 질문에 대한 채택된 답변 개수 조회
     */
    public int countAcceptedAnswersByQuestionId(Long questionId) {
        return communityAnswerRepository.countAcceptedAnswersByQuestionId(questionId);
    }

    /**
     * 질문에 참여한 모든 사용자 조회
     */
    public Set<User> getParticipantsByQuestionId(Long questionId) {
        List<CommunityAnswer> answers = communityAnswerRepository.findAllByCommunityQuestionId(questionId);
        return answers.stream()
                .map(CommunityAnswer::getUser)
                .collect(Collectors.toSet());
    }
}