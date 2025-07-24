package insty.domain.community.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import insty.domain.common.dto.PaginationReq;
import insty.domain.common.dto.PaginationRes;
import insty.domain.community.dto.CommunityQuestionSearchFilter;
import insty.domain.community.repository.CommunityQuestionQueryRepository;
import insty.model.community.CommunityAnswer;
import insty.model.community.CommunityAnswerFixtureBuilder;
import insty.model.community.CommunityFile;
import insty.model.community.CommunityQuestion;
import insty.model.community.CommunityQuestionFixtureBuilder;
import insty.model.file.FileFixtureBuilder;

import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class CommunityComplexReaderTest {

    @InjectMocks
    private CommunityComplexReader communityComplexReader;

    @Mock
    private CommunityQuestionQueryRepository communityQuestionQueryRepository;

    @Test
    void searchQuestions_질문_기본필드_검증() {
        // 질문의 기본 필드들이 올바르게 매핑되는지 검증

        // given
        PaginationReq paginationReq = new PaginationReq(1, 10);
        CommunityQuestionSearchFilter filter = new CommunityQuestionSearchFilter(null, null, null, null);
        String sort = "createdAt:desc";

        CommunityQuestion question = CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser(1L, "질문1", "내용1");
        when(communityQuestionQueryRepository.searchQuestions(paginationReq, filter, sort))
                .thenReturn(List.of(question));

        // when
        List<CommunityQuestion> res = communityComplexReader.searchQuestions(paginationReq, filter, sort);

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
        CommunityQuestionSearchFilter filter = new CommunityQuestionSearchFilter(null, null, null, null);
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
        List<CommunityQuestion> res = communityComplexReader.searchQuestions(paginationReq, filter, sort);

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
        CommunityQuestionSearchFilter filter = new CommunityQuestionSearchFilter(null, null, null, null);
        String sort = "createdAt:desc";

        CommunityQuestion q1 = CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser(1L, "질문1", "내용1");
        CommunityQuestion q2 = CommunityQuestionFixtureBuilder.getCommunityQuestionWithIdAndUser(2L, "질문2", "내용2");
        when(communityQuestionQueryRepository.searchQuestions(paginationReq, filter, sort))
                .thenReturn(List.of(q1, q2));

        // when
        List<CommunityQuestion> res = communityComplexReader.searchQuestions(paginationReq, filter, sort);

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
        // 검색 결과 개수 조회가 올바르게 동작하는지 검증

        // given
        PaginationReq paginationReq = new PaginationReq(1, 10);
        CommunityQuestionSearchFilter filter = new CommunityQuestionSearchFilter(1L, true, "테스트", null);
        PaginationRes paginationRes = new PaginationRes(1, 1, 1, 10);
        when(communityQuestionQueryRepository.countSearchQuestions(paginationReq, filter))
                .thenReturn(paginationRes);

        // when
        PaginationRes res = communityComplexReader.countSearchQuestions(paginationReq, filter);

        // then
        assertThat(res).isNotNull();
        assertThat(res).isEqualTo(paginationRes);
    }
}