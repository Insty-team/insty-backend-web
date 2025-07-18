package insty.domain.community.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import insty.domain.community.dto.CommunityAnswerCreateReq;
import insty.domain.community.dto.CommunityAnswerUpdateReq;
import insty.domain.community.dto.CommunityQuestionCreateReq;
import insty.domain.community.dto.CommunityQuestionUpdateReq;
import insty.domain.community.repository.CommunityAnswerFileRepository;
import insty.domain.community.repository.CommunityAnswerRepository;
import insty.domain.community.repository.CommunityFileRepository;
import insty.domain.community.repository.CommunityQuestionRepository;
import insty.error.CommunityErrorCode;
import insty.exception.CustomException;
import insty.model.community.CommunityAnswer;
import insty.model.community.CommunityAnswerFile;
import insty.model.community.CommunityFile;
import insty.model.community.CommunityQuestion;
import insty.model.course.Course;
import insty.model.user.User;
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
class CommunityWriterTest {

    @InjectMocks
    private CommunityWriter communityWriter;

    @Mock
    private CommunityQuestionRepository communityQuestionRepository;
    @Mock
    private CommunityAnswerRepository communityAnswerRepository;
    @Mock
    private CommunityFileRepository communityFileRepository;
    @Mock
    private CommunityAnswerFileRepository communityAnswerFileRepository;

    @Test
    void saveQuestion_정상() {
        // 커뮤니티 질문 생성 및 저장이 정상 동작한다.
        // given
        User user = mock(User.class);
        Course course = mock(Course.class);
        String title = "제목";
        String content = "내용";
        CommunityQuestionCreateReq req = new CommunityQuestionCreateReq(user.getId(), course.getId(), title, content);
        when(communityQuestionRepository.save(any(CommunityQuestion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        // when
        CommunityQuestion result = communityWriter.saveQuestion(user, course, req);
        // then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo(title);
        assertThat(result.getContent()).isEqualTo(content);
        verify(communityQuestionRepository, times(1)).save(any(CommunityQuestion.class));
    }

    @Test
    void updateQuestion_정상() {
        // 커뮤니티 질문 수정이 정상 동작한다.
        // given
        Long questionId = 1L;
        String title = "수정된 제목";
        String content = "수정된 내용";
        CommunityQuestionUpdateReq req = new CommunityQuestionUpdateReq(questionId, title, content);
        CommunityQuestion question = mock(CommunityQuestion.class);
        when(communityQuestionRepository.findById(questionId)).thenReturn(Optional.of(question));
        when(communityQuestionRepository.save(any(CommunityQuestion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        // when
        CommunityQuestion result = communityWriter.updateQuestion(questionId, req);
        // then
        assertThat(result).isNotNull();
        verify(question, times(1)).update(title, content, question.getAttachments());
        verify(communityQuestionRepository, times(1)).save(question);
    }

    @Test
    void updateQuestion_에러_존재하지않는질문() {
        // 커뮤니티 질문이 존재하지 않을 때 예외가 발생한다.
        // given
        Long questionId = 1L;
        CommunityQuestionUpdateReq req = new CommunityQuestionUpdateReq(questionId,"제목", "내용");
        when(communityQuestionRepository.findById(questionId)).thenReturn(Optional.empty());
        // when & then
        assertThatThrownBy(() -> communityWriter.updateQuestion(questionId, req))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_QUESTION_NOT_FOUND);
    }

    @Test
    void deleteQuestion_정상() {
        // 커뮤니티 질문 삭제가 정상 동작한다.
        // given
        CommunityQuestion question = mock(CommunityQuestion.class);
        // when
        communityWriter.deleteQuestion(question);
        // then
        verify(communityQuestionRepository, times(1)).delete(question);
    }

    @Test
    void saveCommunityFiles_정상() {
        // 커뮤니티 파일 저장이 정상 동작한다.
        // given
        CommunityFile file1 = mock(CommunityFile.class);
        CommunityFile file2 = mock(CommunityFile.class);
        List<CommunityFile> communityFiles = List.of(file1, file2);
        when(communityFileRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        // when
        List<CommunityFile> result = communityWriter.saveCommunityFiles(communityFiles);
        // then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(file1, file2);
        verify(communityFileRepository, times(1)).saveAll(communityFiles);
    }

    @Test
    void deleteCommunityFiles_정상() {
        // 커뮤니티 파일 삭제가 정상 동작한다.
        // given
        CommunityFile file1 = mock(CommunityFile.class);
        CommunityFile file2 = mock(CommunityFile.class);
        List<CommunityFile> communityFiles = List.of(file1, file2);
        // when
        communityWriter.deleteCommunityFiles(communityFiles);
        // then
        verify(communityFileRepository, times(1)).deleteAll(communityFiles);
    }

    @Test
    void saveAnswer_정상() {
        // 커뮤니티 답변 생성 및 저장이 정상 동작한다.
        // given
        User user = mock(User.class);
        CommunityQuestion question = mock(CommunityQuestion.class);
        String content = "답변 내용";
        CommunityAnswerCreateReq req = new CommunityAnswerCreateReq(question.getId(), user.getId(), content, null);
        when(communityAnswerRepository.save(any(CommunityAnswer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        // when
        CommunityAnswer result = communityWriter.saveAnswer(user, question, req);
        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo(content);
        verify(communityAnswerRepository, times(1)).save(any(CommunityAnswer.class));
    }

    @Test
    void updateAnswer_정상() {
        // 커뮤니티 답변 수정이 정상 동작한다.
        // given
        Long answerId = 1L;
        String content = "수정된 답변 내용";
        CommunityAnswerUpdateReq req = CommunityAnswerUpdateReq.create(answerId, content);
        CommunityAnswer answer = mock(CommunityAnswer.class);
        when(communityAnswerRepository.findById(answerId)).thenReturn(Optional.of(answer));
        when(communityAnswerRepository.save(any(CommunityAnswer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        // when
        CommunityAnswer result = communityWriter.updateAnswer(answerId, req);
        // then
        assertThat(result).isNotNull();
        verify(answer, times(1)).update(content);
        verify(communityAnswerRepository, times(1)).save(answer);
    }

    @Test
    void updateAnswer_에러_존재하지않는답변() {
        // 커뮤니티 답변이 존재하지 않을 때 예외가 발생한다.
        // given
        Long answerId = 1L;
        String content = "수정된 답변 내용";
        CommunityAnswerUpdateReq req = CommunityAnswerUpdateReq.create(answerId, content);
        when(communityAnswerRepository.findById(answerId)).thenReturn(Optional.empty());
        // when & then
        assertThatThrownBy(() -> communityWriter.updateAnswer(answerId, req))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_ANSWER_NOT_FOUND);
    }

    @Test
    void deleteAnswer_정상() {
        // 커뮤니티 답변 삭제가 정상 동작한다.
        // given
        CommunityAnswer answer = mock(CommunityAnswer.class);
        // when
        communityWriter.deleteAnswer(answer);
        // then
        verify(communityAnswerRepository, times(1)).delete(answer);
    }

    @Test
    void saveCommunityAnswerFiles_정상() {
        // 커뮤니티 답변 파일 저장이 정상 동작한다.
        // given
        CommunityAnswerFile answerFile1 = mock(CommunityAnswerFile.class);
        CommunityAnswerFile answerFile2 = mock(CommunityAnswerFile.class);
        List<CommunityAnswerFile> communityAnswerFiles = List.of(answerFile1, answerFile2);
        when(communityAnswerFileRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        // when
        List<CommunityAnswerFile> result = communityWriter.saveCommunityAnswerFiles(communityAnswerFiles);
        // then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(answerFile1, answerFile2);
        verify(communityAnswerFileRepository, times(1)).saveAll(communityAnswerFiles);
    }

    @Test
    void saveCommunityAnswerFile_정상() {
        // 커뮤니티 답변 파일 저장이 정상 동작한다.
        // given
        CommunityAnswerFile communityAnswerFile = mock(CommunityAnswerFile.class);
        when(communityAnswerFileRepository.save(any(CommunityAnswerFile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        // when
        CommunityAnswerFile result = communityWriter.saveCommunityAnswerFile(communityAnswerFile);
        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(communityAnswerFile);
        verify(communityAnswerFileRepository, times(1)).save(communityAnswerFile);
    }

    @Test
    void deleteCommunityAnswerFiles_정상() {
        // 커뮤니티 답변 파일 삭제가 정상 동작한다.
        // given
        CommunityAnswerFile answerFile1 = mock(CommunityAnswerFile.class);
        CommunityAnswerFile answerFile2 = mock(CommunityAnswerFile.class);
        List<CommunityAnswerFile> communityAnswerFiles = List.of(answerFile1, answerFile2);
        // when
        communityWriter.deleteCommunityAnswerFiles(communityAnswerFiles);
        // then
        verify(communityAnswerFileRepository, times(1)).deleteAll(communityAnswerFiles);
    }

    @Test
    void acceptAnswer_정상() {
        // 커뮤니티 답변 채택이 정상 동작한다.
        // given
        CommunityQuestion question = mock(CommunityQuestion.class);
        CommunityAnswer answer = mock(CommunityAnswer.class);
        // when
        communityWriter.acceptAnswer(question, answer);
        // then
        verify(question, times(1)).acceptAnswer(answer);
        verify(communityQuestionRepository, times(1)).save(question);
    }

    @Test
    void unacceptAnswer_정상() {
        // 커뮤니티 답변 채택 해제가 정상 동작한다.
        // given
        CommunityQuestion question = mock(CommunityQuestion.class);
        // when
        communityWriter.unacceptAnswer(question);
        // then
        verify(question, times(1)).unacceptAnswer();
        verify(communityQuestionRepository, times(1)).save(question);
    }
}