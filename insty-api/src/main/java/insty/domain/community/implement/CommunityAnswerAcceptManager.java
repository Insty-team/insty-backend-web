package insty.domain.community.implement;

import insty.domain.community.dto.AcceptAnswerResultRes;
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
public class CommunityAnswerAcceptManager {

    private final CommunityQuestionRepository communityQuestionRepository;

    /**
     * 답변 채택/취소 토글 (요구사항에 따라 동작)
     * 1. 아무 답변도 채택되지 않은 경우 → 채택
     * 2. 이미 채택된 답변을 다시 클릭 → 취소
     * 3. 이미 다른 답변이 채택되어 있는데, 다른 답변을 채택 요청 → 에러 409
     * 4. 질문 작성자가 자신이 작성한 답변 채택 -> 에러 400
     */
    public AcceptAnswerResultRes acceptAnswer(CommunityQuestion question, CommunityAnswer answer) {
        if (answer.getUser() == null || question.getUser() == null || 
            answer.getUser().getId().equals(question.getUser().getId())) {
            throw new CustomException(CommunityErrorCode.COMMUNITY_ANSWER_SELF_ACCEPT_NOT_ALLOWED);
        }

        // 교차 검증: 요청한 질문의 답변인지 확인
        if (answer.getCommunityQuestion() == null || !answer.getCommunityQuestion().getId().equals(question.getId())) {
            throw new CustomException(CommunityErrorCode.COMMUNITY_ANSWER_NOT_BELONG_TO_QUESTION);
        }

        CommunityAnswer currentAccepted = question.getAcceptedAnswer();
        if (currentAccepted == null) {
            question.acceptAnswer(answer);
            communityQuestionRepository.save(question);
            return new AcceptAnswerResultRes(answer.getId(), true);
        }
        if (currentAccepted.getId().equals(answer.getId())) {
            question.unacceptAnswer();
            communityQuestionRepository.save(question);
            return new AcceptAnswerResultRes(answer.getId(), false);
        }
        throw new CustomException(CommunityErrorCode.COMMUNITY_ALREADY_ACCEPTED_ANSWER);
    }
}