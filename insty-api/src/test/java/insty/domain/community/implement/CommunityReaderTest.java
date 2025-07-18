package insty.domain.community.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import insty.domain.community.repository.CommunityAnswerFileRepository;
import insty.domain.community.repository.CommunityAnswerRepository;
import insty.domain.community.repository.CommunityQuestionRepository;
import insty.error.CommunityErrorCode;
import insty.exception.CustomException;
import insty.model.community.CommunityAnswer;
import insty.model.community.CommunityAnswerFile;
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
class CommunityReaderTest {

    @InjectMocks
    private CommunityReader communityReader;

    @Mock
    private CommunityQuestionRepository communityQuestionRepository;

    @Mock
    private CommunityAnswerRepository communityAnswerRepository;

    @Mock
    private CommunityAnswerFileRepository communityAnswerFileRepository;

    @Test
    void getAllCommunityQuestions_정상() {
        // given
        CommunityQuestion question1 = mock(CommunityQuestion.class);
        CommunityQuestion question2 = mock(CommunityQuestion.class);
        List<CommunityQuestion> questions = List.of(question1, question2);

        // mock
        when(communityQuestionRepository.findAll()).thenReturn(questions);

        // when
        List<CommunityQuestion> result = communityReader.getAllCommunityQuestions();

        // then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(question1, question2);
    }

    @Test
    void getAllCommunityQuestionsByCourseId_정상() {
        // given
        Long courseId = 1L;
        CommunityQuestion question1 = mock(CommunityQuestion.class);
        CommunityQuestion question2 = mock(CommunityQuestion.class);
        List<CommunityQuestion> questions = List.of(question1, question2);

        // mock
        when(communityQuestionRepository.findAllByCourseId(1L)).thenReturn(questions);

        // when
        List<CommunityQuestion> result = communityReader.getAllCommunityQuestionsByCourseId(courseId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(question1, question2);
    }

    @Test
    void getCommunityQuestionDetailsById_정상() {
        // given
        Long questionId = 1L;
        CommunityQuestion question = mock(CommunityQuestion.class);

        // mock
        when(communityQuestionRepository.findById(1L)).thenReturn(Optional.of(question));

        // when
        CommunityQuestion result = communityReader.getCommunityQuestionDetailsById(questionId);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(question);
    }

    @Test
    void getCommunityQuestionDetailsById_에러_존재하지_않는_질문() {
        // given
        Long questionId = 1L;

        // mock
        when(communityQuestionRepository.findById(1L)).thenReturn(Optional.empty());

        // when

        // then
        assertThatThrownBy(() -> communityReader.getCommunityQuestionDetailsById(questionId))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_QUESTION_NOT_FOUND);
    }

    @Test
    void getCommunityAnswerById_정상() {
        // given
        Long answerId = 1L;
        CommunityAnswer answer = mock(CommunityAnswer.class);

        // mock
        when(communityAnswerRepository.findById(1L)).thenReturn(Optional.of(answer));

        // when
        CommunityAnswer result = communityReader.getCommunityAnswerById(answerId);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(answer);
    }

    @Test
    void getCommunityAnswerById_에러_존재하지_않는_답변() {
        // given
        Long answerId = 1L;

        // mock
        when(communityAnswerRepository.findById(1L)).thenReturn(Optional.empty());

        // when

        // then
        assertThatThrownBy(() -> communityReader.getCommunityAnswerById(answerId))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_ANSWER_NOT_FOUND);
    }

    @Test
    void getAllCommunityAnswers_정상() {
        // given
        Long questionId = 1L;
        CommunityAnswer answer1 = mock(CommunityAnswer.class);
        CommunityAnswer answer2 = mock(CommunityAnswer.class);
        List<CommunityAnswer> answers = List.of(answer1, answer2);

        // mock
        when(communityAnswerRepository.findAllByCommunityQuestionId(1L)).thenReturn(answers);

        // when
        List<CommunityAnswer> result = communityReader.getAllCommunityAnswers(questionId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(answer1, answer2);
    }

    @Test
    void getCommunityAnswerFilesByAnswerId_정상() {
        // given
        Long answerId = 1L;
        CommunityAnswerFile answerFile1 = mock(CommunityAnswerFile.class);
        CommunityAnswerFile answerFile2 = mock(CommunityAnswerFile.class);
        List<CommunityAnswerFile> answerFiles = List.of(answerFile1, answerFile2);

        // mock
        when(communityAnswerFileRepository.findAllByCommunityAnswerId(1L)).thenReturn(answerFiles);

        // when
        List<CommunityAnswerFile> result = communityReader.getCommunityAnswerFilesByAnswerId(answerId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(answerFile1, answerFile2);
    }
}