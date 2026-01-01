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
        for (CourseQuestion question : questions) {
            deleteQuestion(question);
        }
    }

    /**
     * 단일 질문 삭제 시 관련 답변/첨부/영상을 모두 정리한다.
     */
    public void deleteQuestion(CourseQuestion question) {
        deleteAnswers(question);
        courseQuestionFileWriter.deleteQuestionFiles(question);
        courseQuestionVideoManager.deleteQuestionVideo(question);
        courseQuestionRepository.delete(question);
    }

    private void deleteAnswers(CourseQuestion question) {
        List<CourseAnswer> answers = courseAnswerRepository.findAllByCourseQuestionId(question.getId());
        for (CourseAnswer answer : answers) {
            courseAnswerFileWriter.deleteAnswerFiles(answer);
            courseAnswerVideoManager.deleteAnswerVideo(answer);
            courseAnswerRepository.delete(answer);
        }
    }
}
