package insty.domain.courseqna.implement;

import insty.domain.courseqna.dto.CommunityAnswerCreateReq;
import insty.domain.courseqna.dto.CommunityAnswerUpdateReq;
import insty.domain.courseqna.repository.CourseAnswerRepository;
import insty.error.CommunityErrorCode;
import insty.exception.CustomException;
import insty.model.courseqna.CourseAnswer;
import insty.model.courseqna.CourseQuestion;
import insty.model.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CommunityAnswerWriter {

    private final CourseAnswerRepository courseAnswerRepository;

    /**
     * 커뮤니티 답변 생성 및 저장
     */
    public CourseAnswer saveAnswer(User user, CourseQuestion question, CommunityAnswerCreateReq req) {
        CourseAnswer answer = CourseAnswer.create(question, user, req.content());
        return courseAnswerRepository.save(answer);
    }

    /**
     * 커뮤니티 답변 수정 (id로 직접 조회)
     */
    public CourseAnswer updateAnswer(Long answerId, CommunityAnswerUpdateReq req) {
        CourseAnswer answer = courseAnswerRepository.findById(answerId)
                .orElseThrow(() -> new CustomException(CommunityErrorCode.COMMUNITY_ANSWER_NOT_FOUND));
        if (answer.isDeleted()) {
            throw new CustomException(CommunityErrorCode.COMMUNITY_ANSWER_ALREADY_DELETED);
        }
        answer.update(req.content());
        return courseAnswerRepository.save(answer);
    }

    /**
     * 커뮤니티 답변 삭제
     */
    public void deleteAnswer(CourseAnswer courseAnswer) {
        if (courseAnswer.isDeleted()) {
            throw new CustomException(CommunityErrorCode.COMMUNITY_ANSWER_ALREADY_DELETED);
        }
        courseAnswerRepository.delete(courseAnswer);
    }
}
