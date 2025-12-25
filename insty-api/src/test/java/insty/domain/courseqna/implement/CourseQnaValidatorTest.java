package insty.domain.courseqna.implement;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import insty.domain.courseqna.repository.CourseAnswerRepository;
import insty.domain.courseqna.repository.CourseQuestionRepository;
import insty.error.CourseQnaErrorCode;
import insty.exception.CustomException;
import insty.model.courseqna.CourseAnswer;
import insty.model.courseqna.CourseQuestion;
import insty.model.user.User;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class CourseQnaValidatorTest {

    @InjectMocks
    private CourseQnaValidator courseQnaValidator;
    @Mock
    private CourseQuestionRepository courseQuestionRepository;
    @Mock
    private CourseAnswerRepository courseAnswerRepository;

    @Test
    void validateQuestionExists_정상() {
        // given
        CourseQuestion question = mock(CourseQuestion.class);
        when(question.isDeleted()).thenReturn(false);
        when(courseQuestionRepository.findById(1L)).thenReturn(Optional.of(question));
        // when & then
        assertThatCode(() -> courseQnaValidator.validateQuestionExists(1L))
                .doesNotThrowAnyException();
    }

    @Test
    void validateQuestionExists_에러_존재하지않음() {
        when(courseQuestionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseQnaValidator.validateQuestionExists(1L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CourseQnaErrorCode.COURSE_QUESTION_NOT_FOUND);
    }

    @Test
    void validateQuestionExists_에러_삭제됨() {
        CourseQuestion question = mock(CourseQuestion.class);
        when(question.isDeleted()).thenReturn(true);
        when(courseQuestionRepository.findById(1L)).thenReturn(Optional.of(question));

        assertThatThrownBy(() -> courseQnaValidator.validateQuestionExists(1L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CourseQnaErrorCode.COURSE_QUESTION_ALREADY_DELETED);
    }

    @Test
    void validateAnswerExists_정상() {
        CourseAnswer answer = mock(CourseAnswer.class);
        when(answer.isDeleted()).thenReturn(false);
        when(courseAnswerRepository.findById(1L)).thenReturn(Optional.of(answer));

        assertThatCode(() -> courseQnaValidator.validateAnswerExists(1L))
                .doesNotThrowAnyException();
    }

    @Test
    void validateAnswerExists_에러_존재하지않음() {
        when(courseAnswerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseQnaValidator.validateAnswerExists(1L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CourseQnaErrorCode.COURSE_ANSWER_NOT_FOUND);
    }

    @Test
    void validateAnswerExists_에러_삭제됨() {
        CourseAnswer answer = mock(CourseAnswer.class);
        when(answer.isDeleted()).thenReturn(true);
        when(courseAnswerRepository.findById(1L)).thenReturn(Optional.of(answer));

        assertThatThrownBy(() -> courseQnaValidator.validateAnswerExists(1L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CourseQnaErrorCode.COURSE_ANSWER_ALREADY_DELETED);
    }

    @Test
    void validateQuestionAuthor_정상() {
        CourseQuestion question = mock(CourseQuestion.class);
        User user = mock(User.class);
        when(user.getId()).thenReturn(10L);
        when(question.getUser()).thenReturn(user);
        when(courseQuestionRepository.findById(1L)).thenReturn(Optional.of(question));

        assertThatCode(() -> courseQnaValidator.validateQuestionAuthor(10L, 1L))
                .doesNotThrowAnyException();
    }

    @Test
    void validateQuestionAuthor_에러_작성자_아님() {
        CourseQuestion question = mock(CourseQuestion.class);
        User user = mock(User.class);
        when(user.getId()).thenReturn(99L);
        when(question.getUser()).thenReturn(user);
        when(courseQuestionRepository.findById(1L)).thenReturn(Optional.of(question));

        assertThatThrownBy(() -> courseQnaValidator.validateQuestionAuthor(10L, 1L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CourseQnaErrorCode.COURSE_NOT_QUESTION_AUTHOR);
    }

    @Test
    void validateAnswerAuthor_정상() {
        CourseAnswer answer = mock(CourseAnswer.class);
        User user = mock(User.class);
        when(user.getId()).thenReturn(10L);
        when(answer.getUser()).thenReturn(user);

        assertThatCode(() -> courseQnaValidator.validateAnswerAuthor(10L, answer))
                .doesNotThrowAnyException();
    }

    @Test
    void validateAnswerAuthor_에러_작성자_아님() {
        CourseAnswer answer = mock(CourseAnswer.class);
        User user = mock(User.class);
        when(user.getId()).thenReturn(99L);
        when(answer.getUser()).thenReturn(user);

        assertThatThrownBy(() -> courseQnaValidator.validateAnswerAuthor(10L, answer))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CourseQnaErrorCode.COURSE_NOT_ANSWER_AUTHOR);
    }

    @Test
    void validateAnswerBelongsToQuestion_정상() {
        // 답변이 해당 질문에 속하는지 정상적으로 검증한다.
        // given
        CourseQuestion question = mock(CourseQuestion.class);
        CourseAnswer answer = mock(CourseAnswer.class);
        Long questionId = 1L;
        when(question.getId()).thenReturn(questionId);
        when(answer.getCourseQuestion()).thenReturn(question);
        when(answer.getCourseQuestion().getId()).thenReturn(questionId);
        // when & then
        assertThatCode(() -> courseQnaValidator.validateAnswerBelongsToQuestion(answer, question)).doesNotThrowAnyException();
    }

    @Test
    void validateAnswerBelongsToQuestion_에러_답변이_해당_질문에_속하지_않음() {
        // 답변이 해당 질문에 속하지 않을 때 예외가 발생한다.
        // given
        CourseQuestion question = mock(CourseQuestion.class);
        CourseQuestion differentQuestion = mock(CourseQuestion.class);
        CourseAnswer answer = mock(CourseAnswer.class);
        Long questionId = 1L;
        Long differentQuestionId = 2L;
        when(question.getId()).thenReturn(questionId);
        when(differentQuestion.getId()).thenReturn(differentQuestionId);
        when(answer.getCourseQuestion()).thenReturn(differentQuestion);
        // when & then
        assertThatThrownBy(() -> courseQnaValidator.validateAnswerBelongsToQuestion(answer, question))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CourseQnaErrorCode.COURSE_ANSWER_NOT_BELONG_TO_QUESTION);
    }

    @Test
    void validateFiles_정상_파일이_없음() {
        // 파일이 없을 때 정상 동작한다.
        // given
        List<MultipartFile> files = null;
        // when & then
        assertThatCode(() -> courseQnaValidator.validateFiles(files)).doesNotThrowAnyException();
    }

    @Test
    void validateFiles_정상_빈_리스트() {
        // 빈 파일 리스트일 때 정상 동작한다.
        // given
        List<MultipartFile> files = List.of();
        // when & then
        assertThatCode(() -> courseQnaValidator.validateFiles(files)).doesNotThrowAnyException();
    }

    @Test
    void validateFiles_에러_빈_파일() {
        // 빈 파일이 포함되어 있을 때 예외가 발생한다.
        // given
        MultipartFile file = mock(MultipartFile.class);
        List<MultipartFile> files = List.of(file);
        when(file.isEmpty()).thenReturn(true);
        // when & then
        assertThatThrownBy(() -> courseQnaValidator.validateFiles(files))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CourseQnaErrorCode.COURSE_FILE_IS_EMPTY);
    }

    @Test
    void validateAndParseVideoUuid_정상_유효한_UUID() {
        // 유효한 UUID일 때 정상 동작한다.
        // given
        String videoUuid = "00000000-0000-0000-0000-000000000001";
        // when & then
        assertThatCode(() -> courseQnaValidator.validateAndParseVideoUuid(videoUuid)).doesNotThrowAnyException();
    }

    @Test
    void validateAndParseVideoUuid_정상_null() {
        // null일 때 정상 동작한다.
        // given
        String videoUuid = null;
        // when & then
        assertThatCode(() -> courseQnaValidator.validateAndParseVideoUuid(videoUuid)).doesNotThrowAnyException();
    }

    @Test
    void validateAndParseVideoUuid_정상_빈문자열() {
        // 빈 문자열일 때 정상 동작한다.
        // given
        String videoUuid = "";
        // when & then
        assertThatCode(() -> courseQnaValidator.validateAndParseVideoUuid(videoUuid)).doesNotThrowAnyException();
    }

    @Test
    void validateAndParseVideoUuid_에러_잘못된_UUID_형식() {
        // 잘못된 UUID 형식일 때 예외가 발생한다.
        // given
        String videoUuid = "invalid-uuid";
        // when & then
        assertThatThrownBy(() -> courseQnaValidator.validateAndParseVideoUuid(videoUuid))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CourseQnaErrorCode.COURSE_INVALID_VIDEO_UUID);
    }
}