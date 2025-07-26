package insty.domain.community.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import insty.domain.community.dto.CommunityAnswerCreateReq;
import insty.domain.community.dto.CommunityAnswerUpdateReq;
import insty.domain.community.dto.CommunityQuestionUpdateReq;
import insty.domain.community.repository.CommunityAnswerFileRepository;
import insty.domain.community.repository.CommunityAnswerRepository;
import insty.error.CommunityErrorCode;
import insty.exception.CustomException;
import insty.model.community.CommunityAnswer;
import insty.model.community.CommunityQuestion;
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
class CommunityAnswerWriterTest {

    @InjectMocks
    private CommunityAnswerWriter writer;
    @Mock
    private CommunityAnswerRepository answerRepository;
    @Mock
    private CommunityAnswerFileRepository fileRepository;

    @Test
    void saveAnswer_정상() {
        // given
        User user = mock(User.class);
        CommunityQuestion question = mock(CommunityQuestion.class);
        CommunityAnswerCreateReq req = CommunityAnswerCreateReq.builder()
                .questionId(1L).content("내용")
                .build();
        when(answerRepository.save(any(CommunityAnswer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        CommunityAnswer result = writer.saveAnswer(user, question, req);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo("내용");
        verify(answerRepository).save(any(CommunityAnswer.class));
    }

    @Test
    void updateAnswer_정상() {
        // given
        Long id = 1L;
        CommunityAnswerUpdateReq req = CommunityAnswerUpdateReq.builder()
                .content("내용")
                .build();
        CommunityAnswer answer = mock(CommunityAnswer.class);
        when(answerRepository.findById(id)).thenReturn(Optional.of(answer));
        when(answerRepository.save(any(CommunityAnswer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        CommunityAnswer result = writer.updateAnswer(id, req);

        // then
        assertThat(result).isNotNull();
        verify(answer).update("내용");
        verify(answerRepository).save(answer);
    }

    @Test
    void updateAnswer_에러_이미삭제됨() {
        // given
        Long id = 1L;
        CommunityAnswerUpdateReq req = CommunityAnswerUpdateReq.builder()
                .content("내용")
                .build();
        CommunityAnswer answer = mock(CommunityAnswer.class);
        when(answerRepository.findById(id)).thenReturn(Optional.of(answer));
        when(answer.isDeleted()).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> writer.updateAnswer(id, req))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_ANSWER_ALREADY_DELETED);
    }

    @Test
    void updateAnswer_에러_존재하지않음() {
        // given
        Long id = 1L;
        CommunityAnswerUpdateReq req = CommunityAnswerUpdateReq.builder()
                .content("내용")
                .build();
        when(answerRepository.findById(id)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> writer.updateAnswer(id, req))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_ANSWER_NOT_FOUND);
    }

    @Test
    void deleteAnswer_정상() {
        // given
        CommunityAnswer answer = mock(CommunityAnswer.class);

        // when
        writer.deleteAnswer(answer);

        // then
        verify(answer).markAsDeleted();
    }

}