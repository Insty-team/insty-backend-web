package insty.domain.community.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import insty.domain.community.repository.CommunityAnswerFileRepository;
import insty.domain.community.repository.CommunityAnswerRepository;
import insty.error.CommunityErrorCode;
import insty.exception.CustomException;
import insty.model.community.CommunityAnswer;
import insty.model.community.CommunityAnswerFile;
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
class CommunityAnswerReaderTest {

    @InjectMocks
    private CommunityAnswerReader reader;
    @Mock
    private CommunityAnswerRepository answerRepository;

    @Test
    void getAllCommunityAnswers_ByQuestionId_정상() {
        Long questionId = 1L;
        CommunityAnswer a1 = mock(CommunityAnswer.class);
        CommunityAnswer a2 = mock(CommunityAnswer.class);

        // 실제 서비스에서 호출하는 메서드 이름으로 변경
        when(answerRepository.findAllWithDetailsByCommunityQuestionId(questionId)).thenReturn(List.of(a1, a2));

        List<CommunityAnswer> result = reader.getAllCommunityAnswersByQuestionId(questionId);

        assertThat(result).containsExactly(a1, a2);
    }

    @Test
    void getCommunityAnswerById_정상() {
        // given
        Long id = 1L;
        CommunityAnswer answer = mock(CommunityAnswer.class);
        when(answerRepository.findById(id)).thenReturn(Optional.of(answer));
        // when
        CommunityAnswer result = reader.getCommunityAnswerById(id);
        // then
        assertThat(result).isEqualTo(answer);
    }

    @Test
    void getCommunityAnswerById_에러_존재하지않음() {
        // given
        Long id = 1L;
        when(answerRepository.findById(id)).thenReturn(Optional.empty());
        // when & then
        assertThatThrownBy(() -> reader.getCommunityAnswerById(id))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_ANSWER_NOT_FOUND);
    }

    @Test
    void getCommunityAnswerById_에러_삭제된답변() {
        // given
        Long id = 1L;
        CommunityAnswer deletedAnswer = mock(CommunityAnswer.class);
        when(deletedAnswer.isDeleted()).thenReturn(true);
        when(answerRepository.findById(id)).thenReturn(Optional.of(deletedAnswer));

        // when & then
        assertThatThrownBy(() -> reader.getCommunityAnswerById(id))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_ANSWER_ALREADY_DELETED);
    }

    @Test
    void getCommunityAnswerByIdIncludingDeleted_정상() {
        // given
        Long id = 1L;
        CommunityAnswer answer = mock(CommunityAnswer.class);
        when(answerRepository.findById(id)).thenReturn(Optional.of(answer));

        // when
        CommunityAnswer result = reader.getCommunityAnswerByIdIncludingDeleted(id);

        // then
        assertThat(result).isEqualTo(answer);
    }


}