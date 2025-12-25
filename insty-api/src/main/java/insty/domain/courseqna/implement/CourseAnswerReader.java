package insty.domain.courseqna.implement;


import insty.domain.courseqna.repository.CourseAnswerRepository;
import insty.domain.courseqna.repository.CourseAnswerFileRepository;
import insty.error.CommunityErrorCode;
import insty.exception.CustomException;
import insty.model.courseqna.CourseAnswer;
import insty.model.courseqna.CourseAnswerFile;
import insty.model.user.User;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CourseAnswerReader {
    private final CourseAnswerRepository courseAnswerRepository;
    private final CourseAnswerFileRepository courseAnswerFileRepository;

    /**
     * 특정 질문의 모든 답변 조회
     */
    public List<CourseAnswer> getAllCommunityAnswersByQuestionId(Long questionId) {
        List<CourseAnswer> answers = courseAnswerRepository.findAllDetailsWithUserByCourseQuestionId(questionId);

        if (answers.isEmpty()) {
            return answers;
        }

        attachFileDataToAnswers(answers);
        return answers;
    }

    /**
     * 커뮤니티 답변을 페이지네이션으로 조회
     */
    public Page<CourseAnswer> getCommunityAnswersByQuestionIdWithPagination(Long questionId, Pageable pageable) {
        Page<CourseAnswer> answerPage = courseAnswerRepository.findAllDetailsWithUserAttachmentsByCourseQuestionIdWithPagination(questionId, pageable);

        if (answerPage.isEmpty()) {
            return answerPage;
        }

        List<CourseAnswer> answers = answerPage.getContent();
        attachFileDataToAnswers(answers);

        return new PageImpl<>(answers, pageable, answerPage.getTotalElements());
    }

    /**
     * 커뮤니티 답변 상세 조회
     */
    public CourseAnswer getCommunityAnswerById(Long answerId) {
        CourseAnswer answer = courseAnswerRepository.findById(answerId)
                .orElseThrow(() -> new CustomException(CommunityErrorCode.COMMUNITY_ANSWER_NOT_FOUND));

        if (answer.isDeleted()) {
            throw new CustomException(CommunityErrorCode.COMMUNITY_ANSWER_ALREADY_DELETED);
        }

        return answer;
    }

    /**
     * 삭제된 답변도 포함하여 커뮤니티 답변 조회
     */
    public CourseAnswer getCommunityAnswerByIdIncludingDeleted(Long answerId) {
        return courseAnswerRepository.findById(answerId)
                .orElseThrow(() -> new CustomException(CommunityErrorCode.COMMUNITY_ANSWER_NOT_FOUND));
    }

    /**
     * 특정 질문에 대한 활성 답변 개수 조회
     */
    public int countActiveAnswersByQuestionId(Long questionId) {
        return courseAnswerRepository.countByCourseQuestionIdAndIsDeletedFalse(questionId);
    }

    /**
     * 특정 질문에 대한 채택된 답변 개수 조회
     */
    public int countAcceptedAnswersByQuestionId(Long questionId) {
        return courseAnswerRepository.countAcceptedAnswersByQuestionId(questionId);
    }

    /**
     * 질문에 참여한 모든 사용자 조회
     */
    public Set<User> getParticipantsByQuestionId(Long questionId) {
        List<CourseAnswer> answers = courseAnswerRepository.findAllByCourseQuestionId(questionId);
        return answers.stream()
                .map(CourseAnswer::getUser)
                .collect(Collectors.toSet());
    }

    /**
     * 답변 리스트에 첨부파일 데이터를 매핑
     */
    private void attachFileDataToAnswers(List<CourseAnswer> answers) {
        List<Long> answerIds = answers.stream()
                .map(CourseAnswer::getId)
                .toList();

        List<CourseAnswerFile> attachments = courseAnswerFileRepository.findAttachmentsByAnswerIds(answerIds);

        Map<Long, List<CourseAnswerFile>> attachmentMap = attachments.stream()
                .collect(Collectors.groupingBy(att -> att.getCourseAnswer().getId()));

        answers.forEach(answer -> {
            List<CourseAnswerFile> answerAttachments = attachmentMap.getOrDefault(answer.getId(), List.of());
            answer.getAttachments().clear();
            answer.getAttachments().addAll(answerAttachments);
        });
    }

    /**
     * 특정 질문의 채택된 답변 조회
     */
    public List<CourseAnswer> getAcceptedAnswersByQuestionId(Long questionId) {
        return courseAnswerRepository.findAcceptedAnswersByQuestionId(questionId);
    }
}