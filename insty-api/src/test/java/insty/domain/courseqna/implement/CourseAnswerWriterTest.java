package insty.domain.courseqna.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import insty.domain.courseqna.dto.CourseAnswerCreateReq;
import insty.domain.courseqna.dto.CourseAnswerUpdateReq;
import insty.domain.courseqna.repository.CourseAnswerFileRepository;
import insty.domain.courseqna.repository.CourseAnswerRepository;
import insty.error.CourseQnaErrorCode;
import insty.exception.CustomException;
import insty.model.courseqna.CourseAnswer;
import insty.model.courseqna.CourseQuestion;
import insty.model.user.User;
import java.util.Optional;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class CourseAnswerWriterTest {

    @InjectMocks
    private CourseAnswerWriter writer;
    @Mock
    private CourseAnswerRepository answerRepository;
    @Mock
    private CourseAnswerVideoManager courseAnswerVideoManager;
    @Mock
    private CourseAnswerFileWriter courseAnswerFileWriter;
    @Mock
    private CourseAnswerFileRepository fileRepository;

    @Test
    void saveAnswer_정상() {
        // given
        User user = mock(User.class);
        CourseQuestion question = mock(CourseQuestion.class);
        CourseAnswerCreateReq req = new CourseAnswerCreateReq("내용", null);
        when(answerRepository.save(any(CourseAnswer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        CourseAnswer result = writer.saveAnswer(user, question, req);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo("내용");
        verify(answerRepository).save(any(CourseAnswer.class));
    }

    @Test
    void updateAnswer_정상() {
        // given
        Long id = 1L;
        CourseAnswerUpdateReq req = new CourseAnswerUpdateReq("내용", null, null);
        CourseAnswer answer = mock(CourseAnswer.class);
        when(answerRepository.findById(id)).thenReturn(Optional.of(answer));
        when(answerRepository.save(any(CourseAnswer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        CourseAnswer result = writer.updateAnswer(id, req);

        // then
        assertThat(result).isNotNull();
        verify(answer).update("내용");
        verify(answerRepository).save(answer);
    }

    @Test
    void updateAnswer_에러_이미삭제됨() {
        // given
        Long id = 1L;
        CourseAnswerUpdateReq req = new CourseAnswerUpdateReq("내용", null, null);
        CourseAnswer answer = mock(CourseAnswer.class);
        when(answerRepository.findById(id)).thenReturn(Optional.of(answer));
        when(answer.isDeleted()).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> writer.updateAnswer(id, req))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CourseQnaErrorCode.COURSE_ANSWER_ALREADY_DELETED);
    }

    @Test
    void updateAnswer_에러_존재하지않음() {
        // given
        Long id = 1L;
        CourseAnswerUpdateReq req = new CourseAnswerUpdateReq("내용", null, null);
        when(answerRepository.findById(id)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> writer.updateAnswer(id, req))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CourseQnaErrorCode.COURSE_ANSWER_NOT_FOUND);
    }

    @Test
    void deleteAnswer_정상() {
        // given
        CourseAnswer answer = mock(CourseAnswer.class);

        // when
        writer.deleteAnswer(answer);

        // then
        verify(answerRepository).delete(answer);
    }

}