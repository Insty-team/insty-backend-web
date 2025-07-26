package insty.domain.community.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import insty.domain.common.dto.PaginationReq;
import insty.domain.common.dto.PaginationRes;
import insty.domain.community.dto.CommunityQuestionSearchFilter;
import insty.domain.community.repository.CommunityQuestionQueryRepository;
import insty.domain.community.repository.CommunityQuestionRepository;
import insty.error.CommunityErrorCode;
import insty.exception.CustomException;
import insty.model.community.CommunityAnswer;
import insty.model.community.CommunityAnswerFixtureBuilder;
import insty.model.community.CommunityFile;
import insty.model.community.CommunityQuestion;
import insty.model.community.CommunityQuestionFixtureBuilder;
import insty.model.file.FileFixtureBuilder;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class CommunityQuestionReaderTest {

    @InjectMocks
    private CommunityQuestionReader reader;
    @Mock
    private CommunityQuestionRepository repository;
    @Mock
    private CommunityQuestionQueryRepository communityQuestionQueryRepository;


    @Test
    void searchQuestions_질문_기본필드_검증() {
        // 질문의 기본 필드들이 올바르게 매핑되는지 검증

        // given
        PaginationReq paginationReq = new PaginationReq(1, 10);
        CommunityQuestionSearchFilter filter = new CommunityQuestionSearchFilter(null, null, null, null, null);
        String sort = "createdAt:desc";

        CommunityQuestion question = CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser(1L, "질문1", "내용1");
        when(communityQuestionQueryRepository.searchQuestions(paginationReq, filter, sort))
                .thenReturn(List.of(question));

        // when
        List<CommunityQuestion> res = reader.searchQuestions(paginationReq, filter, sort);

        // then
        assertThat(res).hasSize(1);
        assertThat(res.get(0).getTitle()).isEqualTo("질문1");
        assertThat(res.get(0).getContent()).isEqualTo("내용1");
        assertThat(res.get(0).getUser().getId()).isEqualTo(question.getUser().getId());
        assertThat(res.get(0).getCourse().getId()).isEqualTo(question.getCourse().getId());
    }

    @Test
    void searchQuestions_첨부파일_답변_채택답변_포함_검증() {
        // 첨부파일, 답변, 채택답변이 포함된 질문의 복합 필드들이 올바르게 매핑되는지 검증

        // given
        PaginationReq paginationReq = new PaginationReq(1, 10);
        CommunityQuestionSearchFilter filter = new CommunityQuestionSearchFilter(null, null, null, null, null);
        String sort = "createdAt:desc";
        CommunityQuestion question = CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser(2L, "질문2", "내용2");
        CommunityFile file = CommunityFile.create(question, FileFixtureBuilder.getCourseThumbnailWithId());
        ReflectionTestUtils.setField(question, "attachments", List.of(file));
        CommunityAnswer answer = CommunityAnswerFixtureBuilder.getCommunityAnswerWithIdAndUser(question, 200L, "답변내용");
        ReflectionTestUtils.setField(question, "answers", List.of(answer));
        ReflectionTestUtils.setField(question, "acceptedAnswer", answer);
        when(communityQuestionQueryRepository.searchQuestions(paginationReq, filter, sort))
                .thenReturn(List.of(question));

        // when
        List<CommunityQuestion> res = reader.searchQuestions(paginationReq, filter, sort);

        // then
        assertThat(res).hasSize(1);
        assertThat(res.get(0).getAttachments()).isNotNull();
        assertThat(res.get(0).getAnswers()).isNotNull();
        assertThat(res.get(0).getAcceptedAnswer()).isNotNull();
    }


    @Test
    void searchQuestions_여러_질문_여러_필드_검증() {
        // 여러 질문이 올바르게 변환되고 빈 필드들이 정상적으로 처리되는지 검증

        // given
        PaginationReq paginationReq = new PaginationReq(1, 10);
        CommunityQuestionSearchFilter filter = new CommunityQuestionSearchFilter(null, null, null, null, null);
        String sort = "createdAt:desc";

        CommunityQuestion q1 = CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser(1L, "질문1", "내용1");
        CommunityQuestion q2 = CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser(2L, "질문2", "내용2");
        when(communityQuestionQueryRepository.searchQuestions(paginationReq, filter, sort))
                .thenReturn(List.of(q1, q2));

        // when
        List<CommunityQuestion> res = reader.searchQuestions(paginationReq, filter, sort);

        // then
        assertThat(res).hasSize(2);
        assertThat(res.get(0).getTitle()).isEqualTo("질문1");
        assertThat(res.get(1).getTitle()).isEqualTo("질문2");
        assertThat(res.get(0).getAnswers()).isEmpty();
        assertThat(res.get(0).getAnswers()).isEmpty();
        assertThat(res.get(0).getAcceptedAnswer()).isNull();
    }

    @Test
    void countSearchQuestions_정상() {

        // given
        PaginationReq paginationReq = new PaginationReq(1, 10);
        CommunityQuestionSearchFilter filter = new CommunityQuestionSearchFilter("테스트", null, null, null, null);
        PaginationRes paginationRes = new PaginationRes(1, 1, 1, 10);
        when(communityQuestionQueryRepository.countSearchQuestions(paginationReq, filter))
                .thenReturn(paginationRes);

        // when
        PaginationRes res = reader.countSearchQuestions(paginationReq, filter);

        // then
        assertThat(res).isNotNull();
        assertThat(res).isEqualTo(paginationRes);
    }


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
        when(repository.findWithCourseUserAttachmentsById(id)).thenReturn(Optional.of(q));
        // when
        CommunityQuestion result = reader.getCommunityQuestionDetailsById(id);
        // then
        assertThat(result).isEqualTo(q);
    }

    @Test
    void getCommunityQuestionDetailsById_에러_존재하지않음() {
        // given
        Long id = 1L;
        when(repository.findWithCourseUserAttachmentsById(id)).thenReturn(Optional.empty());
        // when & then
        assertThatThrownBy(() -> reader.getCommunityQuestionDetailsById(id))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_QUESTION_NOT_FOUND);
    }

    @Test
    void getCommunityQuestionDetailsById_에러_삭제된질문() {
        // given
        Long id = 1L;
        CommunityQuestion deletedQuestion = mock(CommunityQuestion.class);
        when(deletedQuestion.isDeleted()).thenReturn(true);
        when(repository.findWithCourseUserAttachmentsById(id)).thenReturn(Optional.of(deletedQuestion));

        // when & then
        assertThatThrownBy(() -> reader.getCommunityQuestionDetailsById(id))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_QUESTION_ALREADY_DELETED);
    }

    @Test
    void getCommunityQuestionDetailsByIdIncludingDeleted_정상() {
        // given
        Long id = 1L;
        CommunityQuestion question = mock(CommunityQuestion.class);
        when(repository.findById(id)).thenReturn(Optional.of(question));

        // when
        CommunityQuestion result = reader.getCommunityQuestionDetailsByIdIncludingDeleted(id);

        // then
        assertThat(result).isEqualTo(question);
    }
}