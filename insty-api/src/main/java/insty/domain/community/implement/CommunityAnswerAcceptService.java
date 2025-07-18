package insty.domain.community.implement;

import insty.domain.community.repository.CommunityQuestionRepository;
import insty.error.CommunityErrorCode;
import insty.exception.CustomException;
import insty.model.community.CommunityAnswer;
import insty.model.community.CommunityQuestion;
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
     */
    public void acceptAnswer(CommunityQuestion question, CommunityAnswer answer) {
        CommunityAnswer currentAccepted = question.getAcceptedAnswer();
        // 1. 아무 답변도 채택되지 않은 경우 → 채택
        if (currentAccepted == null) {
            question.acceptAnswer(answer);
            communityQuestionRepository.save(question);
            return;
        }
        // 2. 이미 채택된 답변을 다시 클릭 → 취소
        if (currentAccepted.getId().equals(answer.getId())) {
            question.unacceptAnswer();
            communityQuestionRepository.save(question);
            return;
        }
        // 3. 이미 다른 답변이 채택되어 있는데, 다른 답변을 채택 요청 → 에러
        throw new CustomException(CommunityErrorCode.COMMUNITY_ALREADY_ACCEPTED_ANSWER);
    }
}