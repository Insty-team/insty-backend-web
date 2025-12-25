package insty.domain.courseqna.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import insty.model.courseqna.CourseAnswer;
import org.mockito.ArgumentCaptor;

import insty.domain.common.SearchRes;
import insty.domain.courseqna.dto.CommunityAnswerRes;
import insty.domain.courseqna.dto.CommunityAnswerSearchReq;
import insty.domain.courseqna.service.CommunityAnswerService;
import insty.model.video.VideoAnswer;
import java.util.List;
import java.util.Map;
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
class CourseAnswerSearchManagerTest {

    @InjectMocks
    private CommunityAnswerService communityAnswerService;

    @Mock
    private CommunityValidator communityValidator;
    @Mock
    private CommunityAnswerReader communityAnswerReader;
    @Mock
    private CommunityAnswerVideoManager communityAnswerVideoManager;
    @Mock
    private CommunityAnswerMapper communityAnswerMapper;

    @Test
    void getAnswersByQuestionId_정상() {
        // given
        Long questionId = 1L;
        CommunityAnswerSearchReq req = new CommunityAnswerSearchReq(1, 10);
        
        CourseAnswer answer1 = mock(CourseAnswer.class);
        CourseAnswer answer2 = mock(CourseAnswer.class);
        List<CourseAnswer> answers = List.of(answer1, answer2);
        Page<CourseAnswer> answerPage = new PageImpl<>(answers, PageRequest.of(0, 10), 2);
        
        Map<Long, VideoAnswer> videoMap = Map.of();
        CommunityAnswerRes answerRes1 = mock(CommunityAnswerRes.class);
        CommunityAnswerRes answerRes2 = mock(CommunityAnswerRes.class);
        List<CommunityAnswerRes> answerResList = List.of(answerRes1, answerRes2);

        var captor = ArgumentCaptor.forClass(Pageable.class);
        when(communityAnswerReader.getCommunityAnswersByQuestionIdWithPagination(eq(questionId), captor.capture()))
                .thenReturn(answerPage);
        when(communityAnswerVideoManager.getVideoMapByAnswers(answers)).thenReturn(videoMap);
        when(communityAnswerMapper.toCommunityAnswerResList(answers, videoMap)).thenReturn(answerResList);

        // when
        SearchRes<CommunityAnswerRes> result = communityAnswerService.getAnswersByQuestionId(questionId, req);

        // then
        verify(communityValidator).validateQuestionExists(questionId);
        assertThat(result.items()).containsExactly(answerRes1, answerRes2);
        assertThat(result.pagination().totalItems()).isEqualTo(2);
        assertThat(result.pagination().currentPage()).isEqualTo(1);
        assertThat(result.pagination().perPage()).isEqualTo(10);
        // Pageable 변환 검증: 1 -> 0
        assertThat(captor.getValue().getPageNumber()).isEqualTo(0);
        assertThat(captor.getValue().getPageSize()).isEqualTo(10);
    }

    @Test
    void getAnswersByQuestionId_페이지2_정상() {
        // given
        Long questionId = 1L;
        CommunityAnswerSearchReq req = new CommunityAnswerSearchReq(2, 5);
        
        CourseAnswer answer = mock(CourseAnswer.class);
        List<CourseAnswer> answers = List.of(answer);
        Page<CourseAnswer> answerPage = new PageImpl<>(answers, PageRequest.of(1, 5), 10); // total 10 items
        
        Map<Long, VideoAnswer> videoMap = Map.of();
        CommunityAnswerRes answerRes = mock(CommunityAnswerRes.class);
        List<CommunityAnswerRes> answerResList = List.of(answerRes);

        var captor = ArgumentCaptor.forClass(Pageable.class);
        when(communityAnswerReader.getCommunityAnswersByQuestionIdWithPagination(eq(questionId), captor.capture()))
                .thenReturn(answerPage);
        when(communityAnswerVideoManager.getVideoMapByAnswers(answers)).thenReturn(videoMap);
        when(communityAnswerMapper.toCommunityAnswerResList(answers, videoMap)).thenReturn(answerResList);

        // when
        SearchRes<CommunityAnswerRes> result = communityAnswerService.getAnswersByQuestionId(questionId, req);

        // then
        verify(communityValidator).validateQuestionExists(questionId);
        assertThat(result.items()).containsExactly(answerRes);
        assertThat(result.pagination().totalItems()).isEqualTo(10);
        assertThat(result.pagination().currentPage()).isEqualTo(2);
        assertThat(result.pagination().perPage()).isEqualTo(5);
        assertThat(captor.getValue().getPageNumber()).isEqualTo(1);
        assertThat(captor.getValue().getPageSize()).isEqualTo(5);
    }
}