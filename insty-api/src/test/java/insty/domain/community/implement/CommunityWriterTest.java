package insty.domain.community.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import insty.domain.community.dto.CommunityAnswerReq;
import insty.domain.community.dto.CommunityQuestionReq;
import insty.domain.community.reposiotry.CommunityAnswerFileRepository;
import insty.domain.community.reposiotry.CommunityAnswerRepository;
import insty.domain.community.reposiotry.CommunityFileRepository;
import insty.domain.community.reposiotry.CommunityQuestionRepository;
import insty.model.community.CommunityAnswer;
import insty.model.community.CommunityAnswerFile;
import insty.model.community.CommunityFile;
import insty.model.community.CommunityQuestion;
import insty.model.course.Course;
import insty.model.user.User;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

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
        // given
        CommunityQuestion question = mock(CommunityQuestion.class);
        Course course = mock(Course.class);
        User user = mock(User.class);

        // mock
        when(communityQuestionRepository.save(any(CommunityQuestion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        CommunityQuestion result = communityWriter.saveQuestion(question, course, user);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(question);
        verify(communityQuestionRepository, times(1)).save(question);
    }

    @Test
    void saveCommunityFiles_정상() {
        // given
        CommunityFile file1 = mock(CommunityFile.class);
        CommunityFile file2 = mock(CommunityFile.class);
        List<CommunityFile> communityFiles = List.of(file1, file2);

        // mock
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
    void saveCommunityAnswerFiles_정상() {
        // given
        CommunityAnswerFile answerFile1 = mock(CommunityAnswerFile.class);
        CommunityAnswerFile answerFile2 = mock(CommunityAnswerFile.class);
        List<CommunityAnswerFile> communityAnswerFiles = List.of(answerFile1, answerFile2);

        // mock
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
        // given
        CommunityAnswerFile communityAnswerFile = mock(CommunityAnswerFile.class);

        // mock
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
    void deleteCommunityFiles_정상() {
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
    void deleteCommunityAnswerFiles_정상() {
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
    void updateQuestion_정상() {
        // given
        CommunityQuestion prevQuestion = mock(CommunityQuestion.class);
        CommunityQuestionReq req = CommunityQuestionReq.create(
                1L, 1L, 1L, "수정된 제목", "수정된 내용"
        );
        List<MultipartFile> attachments = List.of();

        // mock
        when(communityQuestionRepository.save(any(CommunityQuestion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        CommunityQuestion result = communityWriter.updateQuestion(prevQuestion, req, attachments);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(prevQuestion);
        verify(prevQuestion, times(1)).update(req.title(), req.content(), prevQuestion.getAttachments());
        verify(communityQuestionRepository, times(1)).save(prevQuestion);
    }

    @Test
    void deleteQuestion_정상() {
        // given
        CommunityQuestion question = mock(CommunityQuestion.class);

        // when
        communityWriter.deleteQuestion(question);

        // then
        verify(communityQuestionRepository, times(1)).delete(question);
    }

    @Test
    void saveAnswer_정상() {
        // given
        CommunityQuestion question = mock(CommunityQuestion.class);
        CommunityAnswerReq req = CommunityAnswerReq.create("1", 1L, "답변 내용");
        User user = mock(User.class);
        CommunityAnswer answer = mock(CommunityAnswer.class);

        // mock
        when(communityAnswerRepository.save(any(CommunityAnswer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        CommunityAnswer result = communityWriter.saveAnswer(question, req, user);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo(req.content());
    }

    @Test
    void updateAnswer_정상() {
        // given
        CommunityAnswer prevAnswer = mock(CommunityAnswer.class);
        CommunityAnswerReq req = CommunityAnswerReq.create("1", 1L, "수정된 답변 내용");

        // mock
        when(communityAnswerRepository.save(any(CommunityAnswer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        CommunityAnswer result = communityWriter.updateAnswer(prevAnswer, req);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(prevAnswer);
        verify(prevAnswer, times(1)).update(req.content());
        verify(communityAnswerRepository, times(1)).save(prevAnswer);
    }

    @Test
    void deleteAnswer_정상() {
        // given
        CommunityAnswer answer = mock(CommunityAnswer.class);

        // when
        communityWriter.deleteAnswer(answer);

        // then
        verify(communityAnswerRepository, times(1)).delete(answer);
    }

    @Test
    void acceptAnswer_정상() {
        // given
        CommunityQuestion question = mock(CommunityQuestion.class);
        CommunityAnswer answer = mock(CommunityAnswer.class);

        // mock
        when(communityQuestionRepository.save(any(CommunityQuestion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        communityWriter.acceptAnswer(question, answer);

        // then
        verify(question, times(1)).acceptAnswer(answer);
        verify(communityQuestionRepository, times(1)).save(question);
    }

    @Test
    void unacceptAnswer_정상() {
        // given
        CommunityQuestion question = mock(CommunityQuestion.class);

        // mock
        when(communityQuestionRepository.save(any(CommunityQuestion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        communityWriter.unacceptAnswer(question);

        // then
        verify(question, times(1)).unacceptAnswer();
        verify(communityQuestionRepository, times(1)).save(question);
    }
}