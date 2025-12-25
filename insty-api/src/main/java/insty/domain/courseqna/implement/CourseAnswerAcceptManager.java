package insty.domain.courseqna.implement;

import insty.domain.courseqna.dto.CourseQnaAcceptAnswerResultRes;
import insty.domain.courseqna.repository.CourseQuestionRepository;
import insty.error.CommunityErrorCode;
import insty.exception.CustomException;
import insty.model.courseqna.CourseAnswer;
import insty.model.courseqna.CourseQuestion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CourseAnswerAcceptManager {

    private final CourseQuestionRepository courseQuestionRepository;

    /**
     * 답변 채택/취소 토글 (요구사항에 따라 동작)
     * 1. 아무 답변도 채택되지 않은 경우 → 채택
     * 2. 이미 채택된 답변을 다시 클릭 → 취소
     * 3. 이미 다른 답변이 채택되어 있는데, 다른 답변을 채택 요청 → 에러 409
     */
    public CourseQnaAcceptAnswerResultRes acceptAnswer(CourseQuestion question, CourseAnswer answer) {
        if (answer.getUser() == null || question.getUser() == null) {
            throw new CustomException(CommunityErrorCode.COURSE_ANSWER_INVALID_USER_ID);
        }
        

        // 교차 검증: 요청한 질문의 답변인지 확인
        if (answer.getCourseQuestion() == null || !answer.getCourseQuestion().getId().equals(question.getId())) {
            throw new CustomException(CommunityErrorCode.COURSE_ANSWER_NOT_BELONG_TO_QUESTION);
        }

        CourseAnswer currentAccepted = question.getAcceptedAnswer();
        if (currentAccepted == null) {
            question.acceptAnswer(answer);
            courseQuestionRepository.save(question);
            return new CourseQnaAcceptAnswerResultRes(answer.getId(), true);
        }
        if (currentAccepted.getId().equals(answer.getId())) {
            question.unacceptAnswer();
            courseQuestionRepository.save(question);
            return new CourseQnaAcceptAnswerResultRes(answer.getId(), false);
        }
        throw new CustomException(CommunityErrorCode.COURSE_ALREADY_ACCEPTED_ANSWER);
    }
}