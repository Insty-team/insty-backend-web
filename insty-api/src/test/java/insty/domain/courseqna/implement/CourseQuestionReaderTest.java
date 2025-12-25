package insty.domain.courseqna.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import insty.domain.common.dto.PaginationReq;
import insty.domain.common.dto.PaginationRes;
import insty.domain.courseqna.dto.CourseQuestionSearchFilter;
import insty.domain.courseqna.dto.CourseQuestionSearchInfo;
import insty.domain.courseqna.repository.CourseQuestionQueryRepository;
import insty.domain.courseqna.repository.CourseQuestionRepository;
import insty.error.CommunityErrorCode;
import insty.exception.CustomException;
import insty.model.courseqna.CourseQuestion;
import insty.model.courseqna.CommunityQuestionFixtureBuilder;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class CourseQuestionReaderTest {

    @InjectMocks
    private CourseQuestionReader reader;
    @Mock
    private CourseQuestionRepository repository;
    @Mock
    private CourseQuestionQueryRepository courseQuestionQueryRepository;


    @Test
    void searchQuestions_질문_기본필드_검증() {
        // 질문의 기본 필드들이 올바르게 매핑되는지 검증

        // given
        PaginationReq paginationReq = new PaginationReq(1, 10);
        CourseQuestionSearchFilter filter = new CourseQuestionSearchFilter(null, null, null, null, null);
        String sort = "createdAt:desc";

        CourseQuestionSearchInfo question = CommunityQuestionFixtureBuilder.getCommunityQuestionSearchInfo(1L, 1L,"질문1", "내용1");
        when(courseQuestionQueryRepository.searchQuestions(paginationReq, filter, sort))
                .thenReturn(List.of(question));

        // when
        List<CourseQuestionSearchInfo> res = reader.searchQuestions(paginationReq, filter, sort);

        // then
        assertThat(res).hasSize(1);
        assertThat(res.get(0).title()).isEqualTo("질문1");
        assertThat(res.get(0).content()).isEqualTo("내용1");
        assertThat(res.get(0).user().id()).isEqualTo(question.user().id());
        assertThat(res.get(0).courseId()).isEqualTo(question.courseId());
    }

    @Test
    void searchQuestions_여러_질문_여러_필드_검증() {
        // 여러 질문이 올바르게 변환되고 빈 필드들이 정상적으로 처리되는지 검증

        // given
        PaginationReq paginationReq = new PaginationReq(1, 10);
        CourseQuestionSearchFilter filter = new CourseQuestionSearchFilter(null,  null, null, null, null);
        String sort = "createdAt:desc";

        CourseQuestionSearchInfo q1 = CommunityQuestionFixtureBuilder.getCommunityQuestionSearchInfo(1L, 1L, "질문1", "내용1");
        CourseQuestionSearchInfo q2 = CommunityQuestionFixtureBuilder.getCommunityQuestionSearchInfo(2L,2L, "질문2", "내용2");
        when(courseQuestionQueryRepository.searchQuestions(paginationReq, filter, sort))
                .thenReturn(List.of(q1, q2));

        // when
        List<CourseQuestionSearchInfo> res = reader.searchQuestions(paginationReq, filter, sort);

        // then
        assertThat(res).hasSize(2);
        assertThat(res.get(0).title()).isEqualTo("질문1");
        assertThat(res.get(1).title()).isEqualTo("질문2");
        assertThat(res.get(0).content()).isEqualTo("내용1");
        assertThat(res.get(0).user().id()).isEqualTo(q1.user().id());
        assertThat(res.get(1).courseId()).isEqualTo(q2.courseId());
    }

    @Test
    void countSearchQuestions_정상() {

        // given
        PaginationReq paginationReq = new PaginationReq(1, 10);
        CourseQuestionSearchFilter filter = new CourseQuestionSearchFilter("테스트", null, null, null, null);
        PaginationRes paginationRes = new PaginationRes(1, 1, 1, 10);
        when(courseQuestionQueryRepository.countSearchQuestions(paginationReq, filter))
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
        CourseQuestion q1 = mock(CourseQuestion.class);
        CourseQuestion q2 = mock(CourseQuestion.class);
        when(repository.findAll()).thenReturn(List.of(q1, q2));
        // when
        List<CourseQuestion> result = reader.getAllCommunityQuestions();
        // then
        assertThat(result).containsExactly(q1, q2);
    }

    @Test
    void getAllCommunityQuestionsByCourseId_정상() {
        // given
        Long courseId = 1L;
        CourseQuestion q1 = mock(CourseQuestion.class);
        when(repository.findAllByCourseId(courseId)).thenReturn(List.of(q1));
        // when
        List<CourseQuestion> result = reader.getAllCommunityQuestionsByCourseId(courseId);
        // then
        assertThat(result).containsExactly(q1);
    }

    @Test
    void getCommunityQuestionWithFilesById_정상() {
        // given
        Long id = 1L;
        CourseQuestion q = mock(CourseQuestion.class);
        when(repository.findDetailsWithUserAttachmentsById(id)).thenReturn(Optional.of(q));
        // when
        CourseQuestion result = reader.getCommunityQuestionWithFilesById(id);
        // then
        assertThat(result).isEqualTo(q);
    }

    @Test
    void getCommunityQuestionWithFilesById_에러_존재하지않음() {
        // given
        Long id = 1L;
        when(repository.findDetailsWithUserAttachmentsById(id)).thenReturn(Optional.empty());
        // when & then
        assertThatThrownBy(() -> reader.getCommunityQuestionWithFilesById(id))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_QUESTION_NOT_FOUND);
    }

    @Test
    void getCommunityQuestionWithFilesById_에러_삭제된질문() {
        // given
        Long id = 1L;
        CourseQuestion deletedQuestion = mock(CourseQuestion.class);
        when(deletedQuestion.isDeleted()).thenReturn(true);
        when(repository.findDetailsWithUserAttachmentsById(id)).thenReturn(Optional.of(deletedQuestion));

        // when & then
        assertThatThrownBy(() -> reader.getCommunityQuestionWithFilesById(id))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_QUESTION_ALREADY_DELETED);
    }

    @Test
    void getCommunityQuestionWithFilesByIdIncludingDeleted_정상() {
        // given
        Long id = 1L;
        CourseQuestion question = mock(CourseQuestion.class);
        when(repository.findById(id)).thenReturn(Optional.of(question));

        // when
        CourseQuestion result = reader.getCommunityQuestionDetailsByIdIncludingDeleted(id);

        // then
        assertThat(result).isEqualTo(question);
    }

    @Test
    void getCountByCourseIds_정상() {
        //given
        List<Long> courseIds = List.of(1L, 2L);
        //mock
        Map<Long, Long> mockMap = Map.of(
                1L, 2L,
                2L, 1L
        );
        when(courseQuestionQueryRepository.countByCourseIds(courseIds)).thenReturn(mockMap);
        //when
        Map<Long, Long> countByCourseIds = reader.getCountByCourseIds(courseIds);
        //then
        assertThat(countByCourseIds).hasSize(2);
        assertThat(countByCourseIds.get(1L)).isEqualTo(2L);
        assertThat(countByCourseIds.get(2L)).isEqualTo(1L);
    }
}