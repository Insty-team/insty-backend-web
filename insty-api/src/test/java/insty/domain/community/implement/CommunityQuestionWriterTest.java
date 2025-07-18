package insty.domain.community.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import insty.domain.community.dto.CommunityQuestionCreateReq;
import insty.domain.community.dto.CommunityQuestionUpdateReq;
import insty.domain.community.repository.CommunityQuestionRepository;
import insty.error.CommunityErrorCode;
import insty.exception.CustomException;
import insty.model.community.CommunityQuestion;
import insty.model.course.Course;
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
class CommunityQuestionWriterTest {

    @InjectMocks
    private CommunityQuestionWriter writer;
    @Mock
    private CommunityQuestionRepository repository;

    @Test
    void saveQuestion_정상() {
        // given
        User user = mock(User.class);
        Course course = mock(Course.class);
        CommunityQuestionCreateReq req = new CommunityQuestionCreateReq(1L, 2L, "title", "content");
        when(repository.save(any(CommunityQuestion.class))).thenAnswer(invocation -> invocation.getArgument(0));
        // when
        CommunityQuestion result = writer.saveQuestion(user, course, req);
        // then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("title");
        assertThat(result.getContent()).isEqualTo("content");
        verify(repository).save(any(CommunityQuestion.class));
    }

    @Test
    void updateQuestion_정상() {
        // given
        Long id = 1L;
        CommunityQuestionUpdateReq req = new CommunityQuestionUpdateReq(id, "title", "content");
        CommunityQuestion question = mock(CommunityQuestion.class);
        when(repository.findById(id)).thenReturn(Optional.of(question));
        when(repository.save(any(CommunityQuestion.class))).thenAnswer(invocation -> invocation.getArgument(0));
        // when
        CommunityQuestion result = writer.updateQuestion(id, req);
        // then
        assertThat(result).isNotNull();
        verify(question).update("title", "content", question.getAttachments());
        verify(repository).save(question);
    }

    @Test
    void updateQuestion_에러_존재하지않음() {
        // given
        Long id = 1L;
        CommunityQuestionUpdateReq req = new CommunityQuestionUpdateReq(id, "title", "content");
        when(repository.findById(id)).thenReturn(Optional.empty());
        // when & then
        assertThatThrownBy(() -> writer.updateQuestion(id, req))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_QUESTION_NOT_FOUND);
    }

    @Test
    void deleteQuestion_정상() {
        // given
        CommunityQuestion question = mock(CommunityQuestion.class);
        // when
        writer.deleteQuestion(question);
        // then
        verify(repository).delete(question);
    }
}