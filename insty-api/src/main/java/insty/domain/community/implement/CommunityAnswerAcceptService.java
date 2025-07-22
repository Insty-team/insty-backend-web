package insty.domain.community.implement;

import insty.domain.community.dto.AcceptAnswerResult;
import insty.domain.community.repository.CommunityQuestionRepository;
import insty.error.CommunityErrorCode;
import insty.exception.CustomException;
import insty.model.community.CommunityAnswer;
import insty.model.community.CommunityQuestion;
import insty.model.user.UserType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CommunityAnswerAcceptService {

    private final CommunityQuestionRepository communityQuestionRepository;

    /**
     * 답변 채택/취소 토글 (요구사항에 따라 동작)
     * 1. 아무 답변도 채택되지 않은 경우 → 채택
     * 2. 이미 채택된 답변을 다시 클릭 → 취소
     * 3. 이미 다른 답변이 채택되어 있는데, 다른 답변을 채택 요청 → 에러 409
     * 4. 크리에이터 답변이 아닌 답변 채택 -> 에러 400
     */
    public AcceptAnswerResult acceptAnswer(CommunityQuestion question, CommunityAnswer answer) {
        // CREATOR 답변만 채택 가능
        if (answer.getUser() == null || answer.getUser().getUserType() != UserType.CREATOR) {
            throw new CustomException(CommunityErrorCode.COMMUNITY_ANSWER_USER_TYPE_INVALID);
        }
        CommunityAnswer currentAccepted = question.getAcceptedAnswer();
        // 1. 아무 답변도 채택되지 않은 경우 → 채택
        if (currentAccepted == null) {
            question.acceptAnswer(answer);
            communityQuestionRepository.save(question);
            return new AcceptAnswerResult(answer.getId(), true);
        }
        // 2. 이미 채택된 답변을 다시 클릭 → 취소
        if (currentAccepted.getId().equals(answer.getId())) {
            question.unacceptAnswer();
            communityQuestionRepository.save(question);
            return new AcceptAnswerResult(answer.getId(), false);
        }
        // 3. 이미 다른 답변이 채택되어 있는데, 다른 답변을 채택 요청 → 에러
        throw new CustomException(CommunityErrorCode.COMMUNITY_ALREADY_ACCEPTED_ANSWER);
    }
}