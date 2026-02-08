package insty.domain.courseqna.implement;

import insty.domain.courseqna.repository.CourseAnswerRepository;
import insty.domain.courseqna.repository.CourseQuestionRepository;
import insty.model.courseqna.CourseAnswer;
import insty.model.courseqna.CourseQuestion;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@RequiredArgsConstructor
public class CourseQnaCleaner {

    private final CourseQuestionRepository courseQuestionRepository;
    private final CourseAnswerRepository courseAnswerRepository;
    private final CourseQuestionFileWriter courseQuestionFileWriter;
    private final CourseAnswerFileWriter courseAnswerFileWriter;
    private final CourseQuestionVideoManager courseQuestionVideoManager;
    private final CourseAnswerVideoManager courseAnswerVideoManager;

    /**
     * 강좌 삭제 시 연관된 질문/답변과 첨부/영상까지 모두 정리한다.
     */
    public void deleteAllByCourseId(Long courseId) {
        List<CourseQuestion> questions = courseQuestionRepository.findAllByCourseId(courseId);
        if (questions.isEmpty()) {
            return;
        }
        List<Long> questionIds = questions.stream().map(CourseQuestion::getId).toList();
        List<CourseAnswer> answers = courseAnswerRepository.findAllByCourseQuestionIdIn(questionIds);
        questions.forEach(question -> {
            if (question.getAcceptedAnswer() != null) {
                question.unacceptAnswer();
            }
        });
        answers.forEach(this::deleteAnswer);
        questions.forEach(this::deleteQuestionOnly);
    }

    /**
     * 단일 질문 삭제 시 관련 답변/첨부/영상을 모두 정리한다.
     */
    public void deleteQuestion(CourseQuestion question) {
        if (question.getAcceptedAnswer() != null) {
            question.unacceptAnswer();
        }
        deleteAnswers(question);
        deleteQuestionOnly(question);
    }

    private void deleteAnswers(CourseQuestion question) {
        List<CourseAnswer> answers = courseAnswerRepository.findAllByCourseQuestionIdIncludingDeleted(question.getId());
        answers.forEach(this::deleteAnswer);
    }

    private void deleteAnswer(CourseAnswer answer) {
        courseAnswerFileWriter.deleteAnswerFiles(answer);
        courseAnswerVideoManager.deleteAnswerVideo(answer);
        courseAnswerRepository.delete(answer);
    }

    private void deleteQuestionOnly(CourseQuestion question) {
        courseQuestionFileWriter.deleteQuestionFiles(question);
        courseQuestionVideoManager.deleteQuestionVideo(question);
        courseQuestionRepository.delete(question);
    }
}
