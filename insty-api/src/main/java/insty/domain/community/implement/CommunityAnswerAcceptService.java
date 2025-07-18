package insty.domain.community.implement;

import insty.domain.community.repository.CommunityQuestionRepository;
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
     * 답변 채택 (이미 채택된 답변이 있으면 409 Conflict)
     */
    public void acceptAnswer(CommunityQuestion question, CommunityAnswer answer) {
        if (question.getAcceptedAnswer() != null) {
            // todo : 예외처리
        }
        question.acceptAnswer(answer);
        communityQuestionRepository.save(question);
    }
}