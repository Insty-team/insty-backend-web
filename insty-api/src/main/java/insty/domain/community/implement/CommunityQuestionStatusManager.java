package insty.domain.community.implement;

import insty.domain.community.repository.CommunityQuestionRepository;
import insty.model.community.CommunityAnswer;
import insty.model.community.CommunityQuestion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@RequiredArgsConstructor
public class CommunityQuestionStatusManager {

    private final CommunityAnswerReader communityAnswerReader;
    private final CommunityQuestionRepository communityQuestionRepository;

    /**
     * 답변 생성 시 질문 상태를 ANSWERED로 변경
     */
    public void updateStatusAfterAnswerCreated(CommunityQuestion question) {
        question.changeStatusByAnswer(true);
        communityQuestionRepository.save(question);
    }

    /**
     * 답변 삭제 시 남은 답변 개수를 확인하여 질문 상태 변경
     */
    public void updateStatusAfterAnswerDeleted(CommunityAnswer deletedAnswer) {
        CommunityQuestion question = deletedAnswer.getCommunityQuestion();
        int remainingAnswers = communityAnswerReader.countActiveAnswersByQuestionId(question.getId()) - 1;
        question.changeStatusByAnswer(remainingAnswers > 0);
        communityQuestionRepository.save(question);
    }
}

