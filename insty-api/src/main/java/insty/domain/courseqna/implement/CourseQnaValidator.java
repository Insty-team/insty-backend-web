package insty.domain.courseqna.implement;

import insty.domain.courseqna.repository.CourseAnswerRepository;
import insty.domain.courseqna.repository.CourseQuestionRepository;
import insty.error.CourseQnaErrorCode;
import insty.exception.CustomException;
import insty.model.courseqna.CourseAnswer;
import insty.model.courseqna.CourseQuestion;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class CourseQnaValidator {

    private final CourseQuestionRepository courseQuestionRepository;
    private final CourseAnswerRepository courseAnswerRepository;
    private final CourseQuestionFileReader courseQuestionFileReader;
    private final CourseAnswerFileReader courseAnswerFileReader;

    private static final int MAX_QUESTION_FILE_COUNT = 2;
    private static final int MAX_ANSWER_FILE_COUNT = 1;

    /**
     * 질문 ID가 유효한지 검증 (존재 여부 + 삭제 여부 확인)
     */
    public void validateQuestionExists(Long questionId) {
        CourseQuestion question = courseQuestionRepository.findById(questionId)
                .orElseThrow(() -> new CustomException(CourseQnaErrorCode.COURSE_QUESTION_NOT_FOUND));

        if (question.isDeleted()) {
            throw new CustomException(CourseQnaErrorCode.COURSE_QUESTION_ALREADY_DELETED);
        }
    }

    /**
     * 답변 ID가 유효한지 검증 (존재 여부 + 삭제 여부 확인)
     */
    public void validateAnswerExists(Long answerId) {
        CourseAnswer answer = courseAnswerRepository.findById(answerId)
                .orElseThrow(() -> new CustomException(CourseQnaErrorCode.COURSE_ANSWER_NOT_FOUND));

        if (answer.isDeleted()) {
            throw new CustomException(CourseQnaErrorCode.COURSE_ANSWER_ALREADY_DELETED);
        }
    }

    /**
     * 질문 작성자 검증
     */
    public void validateQuestionAuthor(Long userId, Long questionId) {
        CourseQuestion question = courseQuestionRepository.findById(questionId)
                .orElseThrow(() -> new CustomException(CourseQnaErrorCode.COURSE_QUESTION_NOT_FOUND));
        Long authorId = question.getUser().getId();
        if (!authorId.equals(userId)) {
            throw new CustomException(CourseQnaErrorCode.COURSE_NOT_QUESTION_AUTHOR);
        }
    }

    /**
     * 답변 작성자 검증
     */
    public void validateAnswerAuthor(Long userId, CourseAnswer answer) {
        Long authorId = answer.getUser().getId();
        if (!authorId.equals(userId)) {
            throw new CustomException(CourseQnaErrorCode.COURSE_NOT_ANSWER_AUTHOR);
        }
    }

    /**
     * 답변이 해당 질문에 속하는지 검증
     */
    public void validateAnswerBelongsToQuestion(CourseAnswer answer, CourseQuestion question) {
        if (!answer.getCourseQuestion().getId().equals(question.getId())) {
            throw new CustomException(CourseQnaErrorCode.COURSE_ANSWER_NOT_BELONG_TO_QUESTION);
        }
    }

    /**
     * 답변 내용이 비어있거나 공백만으로 이루어졌는지 검증
     */
    public void validateContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new CustomException(CourseQnaErrorCode.COURSE_CONTENT_IS_REQUIRED);
        }
    }

    /**
     * 파일 검증
     */
    public void validateFiles(List<MultipartFile> files) {
        if (files == null) {
            return;
        }

        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                throw new CustomException(CourseQnaErrorCode.COURSE_FILE_IS_EMPTY);
            }
            // todo : 파일 크기, 확장자 등 추가 검증 로직
        }
    }

    /**
     * 비디오 UUID 검증 및 파싱
     */
    public UUID validateAndParseVideoUuid(String videoUuid) {
        if (videoUuid == null || videoUuid.trim().isEmpty()) {
            return null;
        }

        try {
            return UUID.fromString(videoUuid);
        } catch (IllegalArgumentException e) {
            throw new CustomException(CourseQnaErrorCode.COURSE_INVALID_VIDEO_UUID);
        }
    }

    /**
     * 질문 생성 시 파일 개수 제한 검증
     */
    public void validateQuestionFileCount(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return;
        }

        int fileCount = (int) files.stream()
                .filter(file -> file != null && !file.isEmpty())
                .count();

        if (fileCount > MAX_QUESTION_FILE_COUNT) {
            throw new CustomException(CourseQnaErrorCode.COURSE_MAX_FILE_COUNT_EXCEEDED);
        }
    }

    /**
     * 질문 업데이트 시 파일 개수 제한 검증
     */
    public void validateQuestionFileCountForUpdate(CourseQuestion question, List<MultipartFile> addFiles, List<Long> deleteFileIds) {
        int currentCount = courseQuestionFileReader.getCurrentFileCount(question.getId());
        int addCount = (addFiles == null) ? 0 : (int) addFiles.stream().filter(f -> f != null && !f.isEmpty()).count();
        int deleteCount = (deleteFileIds == null) ? 0 : deleteFileIds.size();
        int finalCount = currentCount - deleteCount + addCount;

        if (finalCount > MAX_QUESTION_FILE_COUNT) {
            throw new CustomException(CourseQnaErrorCode.COURSE_MAX_FILE_COUNT_EXCEEDED);
        }
    }

    /**
     * 답변 생성 시 파일 개수 제한 검증
     */
    public void validateAnswerFileCount(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return;
        }

        int fileCount = (int) files.stream()
                .filter(file -> file != null && !file.isEmpty())
                .count();

        if (fileCount > MAX_ANSWER_FILE_COUNT) {
            throw new CustomException(CourseQnaErrorCode.COURSE_MAX_FILE_COUNT_EXCEEDED);
        }
    }

    /**
     * 답변 업데이트 시 파일 개수 제한 검증
     */
    public void validateAnswerFileCountForUpdate(CourseAnswer answer, List<MultipartFile> addFiles, List<Long> deleteFileIds) {
        int currentCount = courseAnswerFileReader.getCurrentFileCount(answer.getId());
        int addCount = (addFiles == null) ? 0 : (int) addFiles.stream().filter(f -> f != null && !f.isEmpty()).count();
        int deleteCount = (deleteFileIds == null) ? 0 : deleteFileIds.size();
        int finalCount = currentCount - deleteCount + addCount;

        if (finalCount > MAX_ANSWER_FILE_COUNT) {
            throw new CustomException(CourseQnaErrorCode.COURSE_MAX_FILE_COUNT_EXCEEDED);
        }
    }
}