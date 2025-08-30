package insty.domain.community.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import insty.domain.community.repository.CommunityAnswerRepository;
import insty.error.CommunityErrorCode;
import insty.exception.CustomException;
import insty.model.community.CommunityAnswer;
import insty.model.user.User;
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
        when(answerRepository.findAllDetailsWithUserAttachmentsByCommunityQuestionId(questionId)).thenReturn(List.of(a1, a2));

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
        CommunityAnswer answer = mock(CommunityAnswer.class);
        when(answerRepository.findById(id)).thenReturn(Optional.of(answer));
        when(answer.isDeleted()).thenReturn(true);
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

    @Test
    void getCommunityAnswerByIdIncludingDeleted_에러_존재하지않음() {
        // given
        Long id = 1L;
        when(answerRepository.findById(id)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reader.getCommunityAnswerByIdIncludingDeleted(id))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_ANSWER_NOT_FOUND);
    }

    @Test
    void getCommunityAnswerByIdIncludingDeleted_정상_삭제된답변도조회() {
        // given
        Long id = 1L;
        CommunityAnswer deletedAnswer = mock(CommunityAnswer.class);
        when(answerRepository.findById(id)).thenReturn(Optional.of(deletedAnswer));

        // when
        CommunityAnswer result = reader.getCommunityAnswerByIdIncludingDeleted(id);

        // then
        assertThat(result).isEqualTo(deletedAnswer);
    }

    @Test
    void countActiveAnswersByQuestionId_정상() {
        // given
        Long questionId = 1L;
        when(answerRepository.countByCommunityQuestionIdAndIsDeletedFalse(questionId)).thenReturn(5);
        // when
        int result = reader.countActiveAnswersByQuestionId(questionId);
        // then
        assertThat(result).isEqualTo(5);
    }

    @Test
    void countAcceptedAnswersByQuestionId_정상() {
        // given
        Long questionId = 1L;
        when(answerRepository.countAcceptedAnswersByQuestionId(questionId)).thenReturn(2);
        // when
        int result = reader.countAcceptedAnswersByQuestionId(questionId);
        // then
        assertThat(result).isEqualTo(2);
    }

    @Test
    void getParticipantsByQuestionId_정상() {
        // given
        Long questionId = 1L;
        User user1 = mock(User.class);
        User user2 = mock(User.class);
        User user3 = mock(User.class);
        
        CommunityAnswer answer1 = mock(CommunityAnswer.class);
        CommunityAnswer answer2 = mock(CommunityAnswer.class);
        CommunityAnswer answer3 = mock(CommunityAnswer.class);
        
        when(answer1.getUser()).thenReturn(user1);
        when(answer2.getUser()).thenReturn(user2);
        when(answer3.getUser()).thenReturn(user1); // 중복 사용자
        
        when(answerRepository.findAllByCommunityQuestionId(questionId))
                .thenReturn(List.of(answer1, answer2, answer3));

        // when
        Set<User> result = reader.getParticipantsByQuestionId(questionId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).contains(user1, user2);
        // user3은 포함되지 않음 (answer3이 user1을 사용)
    }

    @Test
    void getParticipantsByQuestionId_빈답변리스트_빈셋반환() {
        // given
        Long questionId = 1L;
        when(answerRepository.findAllByCommunityQuestionId(questionId))
                .thenReturn(List.of());

        // when
        Set<User> result = reader.getParticipantsByQuestionId(questionId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void getCommunityAnswersByQuestionIdWithPagination_정상() {
        // given
        Long questionId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        CommunityAnswer a1 = mock(CommunityAnswer.class);
        CommunityAnswer a2 = mock(CommunityAnswer.class);
        List<CommunityAnswer> answers = List.of(a1, a2);
        Page<CommunityAnswer> answerPage = new PageImpl<>(answers, pageable, 2);

        when(answerRepository.findAllDetailsWithUserAttachmentsByCommunityQuestionIdWithPagination(questionId, pageable))
                .thenReturn(answerPage);

        // when
        Page<CommunityAnswer> result = reader.getCommunityAnswersByQuestionIdWithPagination(questionId, pageable);

        // then
        assertThat(result.getContent()).containsExactly(a1, a2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getNumber()).isEqualTo(pageable.getPageNumber());
        assertThat(result.getSize()).isEqualTo(pageable.getPageSize());
        
        // 저장소 호출 인자 검증
        verify(answerRepository).findAllDetailsWithUserAttachmentsByCommunityQuestionIdWithPagination(questionId, pageable);
    }
}