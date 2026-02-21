package insty.domain.courseqna.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import insty.domain.courseqna.repository.CourseAnswerRepository;
import insty.domain.courseqna.repository.CourseAnswerFileRepository;
import insty.error.CourseQnaErrorCode;
import insty.exception.CustomException;
import insty.model.courseqna.CourseAnswer;
import insty.model.user.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class CourseAnswerReaderTest {

    @InjectMocks
    private CourseAnswerReader reader;
    @Mock
    private CourseAnswerRepository answerRepository;
    @Mock
    private CourseAnswerFileRepository answerFileRepository;

    @Test
    void getAllCommunityAnswers_ByQuestionId_정상() {
        Long questionId = 1L;
        CourseAnswer a1 = mock(CourseAnswer.class);
        CourseAnswer a2 = mock(CourseAnswer.class);
        
        when(a1.getId()).thenReturn(1L);
        when(a2.getId()).thenReturn(2L);
        when(a1.getAttachments()).thenReturn(new ArrayList<>());
        when(a2.getAttachments()).thenReturn(new ArrayList<>());

        when(answerRepository.findAllDetailsWithUserByCourseQuestionId(questionId)).thenReturn(List.of(a1, a2));
        when(answerFileRepository.findAttachmentsByAnswerIds(List.of(1L, 2L))).thenReturn(List.of());

        List<CourseAnswer> result = reader.getAllCourseAnswersByQuestionId(questionId);

        assertThat(result).containsExactly(a1, a2);
    }

    @Test
    void getCourseAnswerById_정상() {
        // given
        Long id = 1L;
        CourseAnswer answer = mock(CourseAnswer.class);
        when(answerRepository.findById(id)).thenReturn(Optional.of(answer));
        // when
        CourseAnswer result = reader.getCourseAnswerById(id);
        // then
        assertThat(result).isEqualTo(answer);
    }

    @Test
    void getCourseAnswerById_에러_존재하지않음() {
        // given
        Long id = 1L;
        when(answerRepository.findById(id)).thenReturn(Optional.empty());
        // when & then
        assertThatThrownBy(() -> reader.getCourseAnswerById(id))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CourseQnaErrorCode.COURSE_QNA_ANSWER_NOT_FOUND);
    }

    @Test
    void getCourseAnswerById_에러_삭제된답변() {
        // given
        Long id = 1L;
        CourseAnswer answer = mock(CourseAnswer.class);
        when(answerRepository.findById(id)).thenReturn(Optional.of(answer));
        when(answer.isDeleted()).thenReturn(true);
        // when & then
        assertThatThrownBy(() -> reader.getCourseAnswerById(id))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CourseQnaErrorCode.COURSE_QNA_ANSWER_ALREADY_DELETED);
    }

    @Test
    void getQuestionIdByAnswerId_정상() {
        // given
        Long answerId = 11L;
        Long questionId = 7L;
        when(answerRepository.findQuestionIdByAnswerId(answerId)).thenReturn(Optional.of(questionId));

        // when
        Long result = reader.getQuestionIdByAnswerId(answerId);

        // then
        assertThat(result).isEqualTo(questionId);
    }

    @Test
    void getQuestionIdByAnswerId_에러_존재하지않음() {
        // given
        Long answerId = 11L;
        when(answerRepository.findQuestionIdByAnswerId(answerId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reader.getQuestionIdByAnswerId(answerId))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CourseQnaErrorCode.COURSE_QNA_ANSWER_NOT_FOUND);
    }

    @Test
    void countActiveAnswersByQuestionId_정상() {
        // given
        Long questionId = 1L;
        when(answerRepository.countByCourseQuestionIdAndIsDeletedFalse(questionId)).thenReturn(5);
        // when
        int result = reader.countActiveAnswersByQuestionId(questionId);
        // then
        assertThat(result).isEqualTo(5);
    }

    @Test
    void getParticipantsByQuestionId_정상() {
        // given
        Long questionId = 1L;
        User user1 = mock(User.class);
        User user2 = mock(User.class);
        
        CourseAnswer answer1 = mock(CourseAnswer.class);
        CourseAnswer answer2 = mock(CourseAnswer.class);
        CourseAnswer answer3 = mock(CourseAnswer.class);
        
        when(answer1.getUser()).thenReturn(user1);
        when(answer2.getUser()).thenReturn(user2);
        when(answer3.getUser()).thenReturn(user1); // 중복 사용자
        
        when(answerRepository.findAllByCourseQuestionId(questionId))
                .thenReturn(List.of(answer1, answer2, answer3));

        // when
        Set<User> result = reader.getParticipantsByQuestionId(questionId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).contains(user1, user2);
    }

    @Test
    void getParticipantsByQuestionId_빈답변리스트_빈셋반환() {
        // given
        Long questionId = 1L;
        when(answerRepository.findAllByCourseQuestionId(questionId))
                .thenReturn(List.of());

        // when
        Set<User> result = reader.getParticipantsByQuestionId(questionId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void getAcceptedAnswersByQuestionId_정상() {
        // given
        Long questionId = 1L;
        CourseAnswer acceptedAnswer1 = mock(CourseAnswer.class);
        CourseAnswer acceptedAnswer2 = mock(CourseAnswer.class);

        when(answerRepository.findAcceptedAnswersByQuestionId(questionId))
                .thenReturn(List.of(acceptedAnswer1, acceptedAnswer2));

        // when
        List<CourseAnswer> result = reader.getAcceptedAnswersByQuestionId(questionId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(acceptedAnswer1, acceptedAnswer2);
        verify(answerRepository).findAcceptedAnswersByQuestionId(questionId);
    }

    @Test
    void getAcceptedAnswersByQuestionId_빈결과() {
        // given
        Long questionId = 1L;
        when(answerRepository.findAcceptedAnswersByQuestionId(questionId))
                .thenReturn(List.of());

        // when
        List<CourseAnswer> result = reader.getAcceptedAnswersByQuestionId(questionId);

        // then
        assertThat(result).isEmpty();
        verify(answerRepository).findAcceptedAnswersByQuestionId(questionId);
    }

    @Test
    void getCourseAnswersByQuestionIdWithPagination_정상() {
        // given
        Long questionId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        CourseAnswer a1 = mock(CourseAnswer.class);
        CourseAnswer a2 = mock(CourseAnswer.class);
        List<CourseAnswer> answers = List.of(a1, a2);
        Page<CourseAnswer> answerPage = new PageImpl<>(answers, pageable, 2);

        when(answerRepository.findAllDetailsWithUserAttachmentsByCourseQuestionIdWithPagination(questionId, pageable))
                .thenReturn(answerPage);

        // when
        Page<CourseAnswer> result = reader.getCourseAnswersByQuestionIdWithPagination(questionId, pageable);

        // then
        assertThat(result.getContent()).containsExactly(a1, a2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getNumber()).isEqualTo(pageable.getPageNumber());
        assertThat(result.getSize()).isEqualTo(pageable.getPageSize());
        
        // 저장소 호출 인자 검증
        verify(answerRepository).findAllDetailsWithUserAttachmentsByCourseQuestionIdWithPagination(questionId, pageable);
    }
}
