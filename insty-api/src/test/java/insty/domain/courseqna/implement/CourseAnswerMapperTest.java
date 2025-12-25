package insty.domain.courseqna.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import insty.domain.common.FileInfo;
import insty.domain.courseqna.dto.CommunityAnswerRes;
import insty.model.courseqna.CourseAnswer;
import insty.model.user.User;
import insty.model.user.UserType;
import insty.model.video.VideoAnswer;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class CourseAnswerMapperTest {

    @InjectMocks
    private CommunityAnswerMapper mapper;
    @Mock
    private CommunityAnswerFileReader communityAnswerFileReader;

    @Test
    void toCommunityAnswerResList_정상_비디오존재() {
        // given
        CourseAnswer answer1 = mock(CourseAnswer.class);
        CourseAnswer answer2 = mock(CourseAnswer.class);
        User user1 = mock(User.class);
        User user2 = mock(User.class);
        
        when(answer1.getId()).thenReturn(1L);
        when(answer2.getId()).thenReturn(2L);
        when(answer1.getUser()).thenReturn(user1);
        when(answer2.getUser()).thenReturn(user2);
        when(user1.getId()).thenReturn(10L);
        when(user1.getNickname()).thenReturn("user1");
        when(user1.getUserType()).thenReturn(UserType.LEARNER);
        when(user2.getId()).thenReturn(20L);
        when(user2.getNickname()).thenReturn("user2");
        when(user2.getUserType()).thenReturn(UserType.CREATOR);

        VideoAnswer video1 = mock(VideoAnswer.class);
        VideoAnswer video2 = mock(VideoAnswer.class);
        Map<Long, VideoAnswer> videoMap = Map.of(1L, video1, 2L, video2);

        List<FileInfo> fileInfos1 = List.of(mock(FileInfo.class));
        List<FileInfo> fileInfos2 = List.of(mock(FileInfo.class));
        when(communityAnswerFileReader.getAnswerFileInfos(answer1)).thenReturn(fileInfos1);
        when(communityAnswerFileReader.getAnswerFileInfos(answer2)).thenReturn(fileInfos2);

        List<CourseAnswer> answers = List.of(answer1, answer2);

        // when
        List<CommunityAnswerRes> result = mapper.toCommunityAnswerResList(answers, videoMap);

        // then
        assertThat(result).hasSize(2);
        verify(communityAnswerFileReader).getAnswerFileInfos(answer1);
        verify(communityAnswerFileReader).getAnswerFileInfos(answer2);
    }

    @Test
    void toCommunityAnswerResList_정상_비디오없음() {
        // given
        CourseAnswer answer = mock(CourseAnswer.class);
        User user = mock(User.class);
        
        when(answer.getId()).thenReturn(1L);
        when(answer.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(10L);
        when(user.getNickname()).thenReturn("user");
        when(user.getUserType()).thenReturn(UserType.LEARNER);

        Map<Long, VideoAnswer> videoMap = Map.of();

        List<FileInfo> fileInfos = List.of(mock(FileInfo.class));
        when(communityAnswerFileReader.getAnswerFileInfos(answer)).thenReturn(fileInfos);

        List<CourseAnswer> answers = List.of(answer);

        // when
        List<CommunityAnswerRes> result = mapper.toCommunityAnswerResList(answers, videoMap);

        // then
        assertThat(result).hasSize(1);
        verify(communityAnswerFileReader).getAnswerFileInfos(answer);
    }

    @Test
    void toCommunityAnswerResList_정상_빈리스트() {
        // given
        List<CourseAnswer> answers = List.of();
        Map<Long, VideoAnswer> videoMap = Map.of();

        // when
        List<CommunityAnswerRes> result = mapper.toCommunityAnswerResList(answers, videoMap);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void toCommunityAnswerResList_정상_일부비디오만존재() {
        // given
        CourseAnswer answer1 = mock(CourseAnswer.class);
        CourseAnswer answer2 = mock(CourseAnswer.class);
        User user1 = mock(User.class);
        User user2 = mock(User.class);
        
        when(answer1.getId()).thenReturn(1L);
        when(answer2.getId()).thenReturn(2L);
        when(answer1.getUser()).thenReturn(user1);
        when(answer2.getUser()).thenReturn(user2);
        when(user1.getId()).thenReturn(10L);
        when(user1.getNickname()).thenReturn("user1");
        when(user1.getUserType()).thenReturn(UserType.LEARNER);
        when(user2.getId()).thenReturn(20L);
        when(user2.getNickname()).thenReturn("user2");
        when(user2.getUserType()).thenReturn(UserType.CREATOR);

        VideoAnswer video1 = mock(VideoAnswer.class);
        Map<Long, VideoAnswer> videoMap = Map.of(1L, video1); // answer2는 비디오 없음

        List<FileInfo> fileInfos1 = List.of(mock(FileInfo.class));
        List<FileInfo> fileInfos2 = List.of(mock(FileInfo.class));
        when(communityAnswerFileReader.getAnswerFileInfos(answer1)).thenReturn(fileInfos1);
        when(communityAnswerFileReader.getAnswerFileInfos(answer2)).thenReturn(fileInfos2);

        List<CourseAnswer> answers = List.of(answer1, answer2);

        // when
        List<CommunityAnswerRes> result = mapper.toCommunityAnswerResList(answers, videoMap);

        // then
        assertThat(result).hasSize(2);
        verify(communityAnswerFileReader).getAnswerFileInfos(answer1);
        verify(communityAnswerFileReader).getAnswerFileInfos(answer2);
    }
}
