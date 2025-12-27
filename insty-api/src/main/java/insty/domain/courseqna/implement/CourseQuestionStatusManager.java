package insty.domain.courseqna.implement;

import insty.domain.courseqna.repository.CourseQuestionRepository;
import insty.model.courseqna.CourseAnswer;
import insty.model.courseqna.CourseQuestion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@RequiredArgsConstructor
public class CourseQuestionStatusManager {

    private final CourseAnswerReader courseAnswerReader;
    private final CourseQuestionRepository courseQuestionRepository;

    /**
     * 답변 생성 시 질문 상태를 ANSWERED로 변경
     */
    public void updateStatusAfterAnswerCreated(CourseQuestion question) {
        question.changeStatusByAnswer(true);
        courseQuestionRepository.save(question);
    }

    /**
     * 답변 삭제 시 남은 답변 개수를 확인하여 질문 상태 변경
     * 채택된 답변이 삭제되는 경우 특별 처리
     */
    public void updateStatusAfterAnswerDeleted(CourseAnswer deletedAnswer) {
        CourseQuestion question = deletedAnswer.getCourseQuestion();
        int remainingAnswers = courseAnswerReader.countActiveAnswersByQuestionId(question.getId()) - 1;
        
        // 채택된 답변이 삭제되는 경우
        if (deletedAnswer.isAccepted()) {
            question.handleAcceptedAnswerDeleted(remainingAnswers > 0);
        } else {
            // 일반 답변 삭제
            question.changeStatusByAnswer(remainingAnswers > 0);
        }
        
        courseQuestionRepository.save(question);
    }
}
