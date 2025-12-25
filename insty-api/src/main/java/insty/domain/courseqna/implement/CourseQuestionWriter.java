package insty.domain.courseqna.implement;

import insty.domain.courseqna.dto.CourseQuestionCreateReq;
import insty.domain.courseqna.dto.CourseQuestionUpdateReq;
import insty.domain.courseqna.repository.CourseQuestionRepository;
import insty.error.CourseQnaErrorCode;
import insty.exception.CustomException;
import insty.model.courseqna.CourseQuestion;
import insty.model.course.Course;
import insty.model.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CourseQuestionWriter {

    private final CourseQuestionRepository courseQuestionRepository;

    /**
     * 커뮤니티 질문 생성 및 저장
     */
    public CourseQuestion saveQuestion(User user, Course course, CourseQuestionCreateReq req) {
        CourseQuestion question = CourseQuestion.create(
                course,
                user,
                req.title(),
                req.content()
        );
        return courseQuestionRepository.save(question);
    }

    /**
     * 커뮤니티 질문 수정 (id로 직접 조회)
     */
    public CourseQuestion updateQuestion(Long questionId, CourseQuestionUpdateReq req) {
        CourseQuestion question = courseQuestionRepository.findById(questionId)
                .orElseThrow(() -> new CustomException(CourseQnaErrorCode.COURSE_QUESTION_NOT_FOUND));
        if (question.isDeleted()) {
            throw new CustomException(CourseQnaErrorCode.COURSE_QUESTION_ALREADY_DELETED);
        }
        question.update(req.title(), req.content());
        return courseQuestionRepository.save(question);
    }

    /**
     * 커뮤니티 질문 삭제
     */
    public void deleteQuestion(CourseQuestion courseQuestion) {
        if (courseQuestion.isDeleted()) {
            throw new CustomException(CourseQnaErrorCode.COURSE_QUESTION_ALREADY_DELETED);
        }
        courseQuestionRepository.delete(courseQuestion);
    }

}
