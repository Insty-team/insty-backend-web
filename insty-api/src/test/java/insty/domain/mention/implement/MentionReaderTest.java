package insty.domain.mention.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import insty.domain.mention.repository.MentionRepository;
import insty.domain.user.repository.UserRepository;
import insty.model.mention.Mention;
import insty.model.mention.MentionFixtureBuilder;
import insty.model.mention.MentionTargetType;
import insty.model.user.User;
import insty.model.user.UserFixtureBuilder;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class MentionReaderTest {

    @InjectMocks
    private MentionReader mentionReader;

    @Mock
    private UserRepository userRepository;
    @Mock
    private MentionRepository mentionRepository;

    @Test
    void searchMentionableUsers_정상() {
        // given
        int size = 10;
        String searchKeyword = "홍길동";
        Long excludedUserId = 1L;
        Pageable pageable = PageRequest.of(0, size);
        
        User user1 = UserFixtureBuilder.getUserWithId(2L);
        User user2 = UserFixtureBuilder.getUserWithId(3L);
        List<User> expectedUsers = List.of(user1, user2);

        // mock
        when(userRepository.searchUsersByKeyword(searchKeyword, excludedUserId, pageable))
                .thenReturn(expectedUsers);

        // when
        List<User> result = mentionReader.searchMentionableUsers(size, searchKeyword, excludedUserId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(user1, user2);
    }

    @Test
    void searchMentionableUsers_검색_결과_없음() {
        // given
        int size = 10;
        String searchKeyword = "존재하지않는사용자";
        Long excludedUserId = 1L;
        Pageable pageable = PageRequest.of(0, size);

        // mock
        when(userRepository.searchUsersByKeyword(searchKeyword, excludedUserId, pageable))
                .thenReturn(List.of());

        // when
        List<User> result = mentionReader.searchMentionableUsers(size, searchKeyword, excludedUserId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void getMentionsByAnswerId_정상() {
        // given
        Long answerId = 1L;
        Mention mention1 = MentionFixtureBuilder.getMentionWithId(1L);
        Mention mention2 = MentionFixtureBuilder.getMentionWithId(2L);
        List<Mention> expectedMentions = List.of(mention1, mention2);

        // mock
        when(mentionRepository.findAllByTargetTypeAndTargetId(MentionTargetType.COURSE_ANSWER, answerId))
                .thenReturn(expectedMentions);

        // when
        List<Mention> result = mentionReader.getMentionsByAnswerId(answerId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(mention1, mention2);
    }

    @Test
    void getMentionsByAnswerId_멘션_없음() {
        // given
        Long answerId = 1L;

        // mock
        when(mentionRepository.findAllByTargetTypeAndTargetId(MentionTargetType.COURSE_ANSWER, answerId))
                .thenReturn(List.of());

        // when
        List<Mention> result = mentionReader.getMentionsByAnswerId(answerId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void getMentionsByAnswerId_answerId가Null이면_빈리스트반환() {
        // when
        List<Mention> result = mentionReader.getMentionsByAnswerId(null);

        // then
        assertThat(result).isEmpty();
        verify(mentionRepository, never()).findAllByTargetTypeAndTargetId(MentionTargetType.COURSE_ANSWER, null);
    }

    @Test
    void getMentionsByTarget_타겟파라미터가Null이면_빈리스트반환() {
        // when
        List<Mention> result1 = mentionReader.getMentionsByTarget(null, 1L);
        List<Mention> result2 = mentionReader.getMentionsByTarget(MentionTargetType.COURSE_ANSWER, null);

        // then
        assertThat(result1).isEmpty();
        assertThat(result2).isEmpty();
        verify(mentionRepository, never()).findAllByTargetTypeAndTargetId(null, 1L);
        verify(mentionRepository, never()).findAllByTargetTypeAndTargetId(MentionTargetType.COURSE_ANSWER, null);
    }

    @Test
    void getMentionsByTarget_정상() {
        // given
        Mention mention1 = MentionFixtureBuilder.getMentionWithId(1L);
        Mention mention2 = MentionFixtureBuilder.getMentionWithId(2L);
        List<Mention> expectedMentions = List.of(mention1, mention2);

        when(mentionRepository.findAllByTargetTypeAndTargetId(MentionTargetType.COMMUNITY_POST, 10L))
                .thenReturn(expectedMentions);

        // when
        List<Mention> result = mentionReader.getMentionsByTarget(MentionTargetType.COMMUNITY_POST, 10L);

        // then
        assertThat(result).containsExactlyInAnyOrder(mention1, mention2);
    }
}
