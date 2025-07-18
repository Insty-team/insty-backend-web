package insty.domain.community.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import insty.domain.community.repository.CommunityQuestionRepository;
import insty.error.CommunityErrorCode;
import insty.exception.CustomException;
import insty.model.community.CommunityQuestion;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class CommunityQuestionReaderTest {

    @InjectMocks
    private CommunityQuestionReader reader;
    @Mock
    private CommunityQuestionRepository repository;

    @Test
    void getAllCommunityQuestions_정상() {
        // given
        CommunityQuestion q1 = mock(CommunityQuestion.class);
        CommunityQuestion q2 = mock(CommunityQuestion.class);
        when(repository.findAll()).thenReturn(List.of(q1, q2));
        // when
        List<CommunityQuestion> result = reader.getAllCommunityQuestions();
        // then
        assertThat(result).containsExactly(q1, q2);
    }

    @Test
    void getAllCommunityQuestionsByCourseId_정상() {
        // given
        Long courseId = 1L;
        CommunityQuestion q1 = mock(CommunityQuestion.class);
        when(repository.findAllByCourseId(courseId)).thenReturn(List.of(q1));
        // when
        List<CommunityQuestion> result = reader.getAllCommunityQuestionsByCourseId(courseId);
        // then
        assertThat(result).containsExactly(q1);
    }

    @Test
    void getCommunityQuestionDetailsById_정상() {
        // given
        Long id = 1L;
        CommunityQuestion q = mock(CommunityQuestion.class);
        when(repository.findById(id)).thenReturn(Optional.of(q));
        // when
        CommunityQuestion result = reader.getCommunityQuestionDetailsById(id);
        // then
        assertThat(result).isEqualTo(q);
    }

    @Test
    void getCommunityQuestionDetailsById_에러_존재하지않음() {
        // given
        Long id = 1L;
        when(repository.findById(id)).thenReturn(Optional.empty());
        // when & then
        assertThatThrownBy(() -> reader.getCommunityQuestionDetailsById(id))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_QUESTION_NOT_FOUND);
    }
}