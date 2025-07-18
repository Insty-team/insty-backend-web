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
    @Mock
    private CommunityAnswerFileRepository fileRepository;

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
    void getAllCommunityAnswers_정상() {
        // given
        Long questionId = 1L;
        CommunityAnswer a1 = mock(CommunityAnswer.class);
        CommunityAnswer a2 = mock(CommunityAnswer.class);
        when(answerRepository.findAllByCommunityQuestionId(questionId)).thenReturn(List.of(a1, a2));
        // when
        List<CommunityAnswer> result = reader.getAllCommunityAnswers(questionId);
        // then
        assertThat(result).containsExactly(a1, a2);
    }

    @Test
    void getCommunityAnswerFilesByAnswerId_정상() {
        // given
        Long answerId = 1L;
        CommunityAnswerFile f1 = mock(CommunityAnswerFile.class);
        CommunityAnswerFile f2 = mock(CommunityAnswerFile.class);
        when(fileRepository.findAllByCommunityAnswerId(answerId)).thenReturn(List.of(f1, f2));
        // when
        List<CommunityAnswerFile> result = reader.getCommunityAnswerFilesByAnswerId(answerId);
        // then
        assertThat(result).containsExactly(f1, f2);
    }
}