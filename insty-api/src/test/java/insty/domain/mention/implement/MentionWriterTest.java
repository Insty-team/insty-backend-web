package insty.domain.mention.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import insty.domain.mention.dto.MentionedUserInfo;
import insty.domain.mention.repository.MentionRepository;
import insty.domain.user.repository.UserRepository;
import insty.error.MentionErrorCode;
import insty.exception.CustomException;
import insty.model.community.CommunityAnswer;
import insty.model.community.CommunityAnswerFixtureBuilder;
import insty.model.mention.Mention;
import insty.model.mention.MentionFixtureBuilder;
import insty.model.user.User;
import insty.model.user.UserFixtureBuilder;
import java.time.Instant;
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
class MentionWriterTest {

    @InjectMocks
    private MentionWriter mentionWriter;

    @Mock
    private MentionRepository mentionRepository;
    @Mock
    private UserRepository userRepository;

    @Test
    void saveMentions_정상() {
        // given
        User mentionerUser = UserFixtureBuilder.getUserWithId(1L);
        User mentionedUser1 = UserFixtureBuilder.getUserWithId(2L);
        User mentionedUser2 = UserFixtureBuilder.getUserWithId(3L);
        CommunityAnswer communityAnswer = CommunityAnswerFixtureBuilder.getCommunityAnswerWithId(1L);
        
        MentionedUserInfo userInfo1 = new MentionedUserInfo(2L, "홍길동");
        MentionedUserInfo userInfo2 = new MentionedUserInfo(3L, "김철수");
        List<MentionedUserInfo> mentionedUserInfos = List.of(userInfo1, userInfo2);

        Mention mention1 = MentionFixtureBuilder.getMentionWithId(1L);
        Mention mention2 = MentionFixtureBuilder.getMentionWithId(2L);

        // mock
        when(userRepository.findAllById(java.util.Set.of(2L, 3L))).thenReturn(List.of(mentionedUser1, mentionedUser2));
        when(mentionRepository.save(any(Mention.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        List<Mention> result = mentionWriter.saveMentions(mentionedUserInfos, mentionerUser, communityAnswer);

        // then
        assertThat(result).hasSize(2);
    }

    @Test
    void saveMentions_멘션된_사용자_없음() {
        // given
        User mentionerUser = UserFixtureBuilder.getUserWithId(1L);
        CommunityAnswer communityAnswer = CommunityAnswerFixtureBuilder.getCommunityAnswerWithId(1L);
        
        MentionedUserInfo userInfo = new MentionedUserInfo(999L, "존재하지않는사용자");
        List<MentionedUserInfo> mentionedUserInfos = List.of(userInfo);

        // mock
        when(userRepository.findAllById(java.util.Set.of(999L))).thenReturn(List.of());

        // when & then
        assertThatThrownBy(() -> mentionWriter.saveMentions(mentionedUserInfos, mentionerUser, communityAnswer))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(MentionErrorCode.MENTION_USER_NOT_FOUND);
    }

    @Test
    void validateMentionCooldown_정상() {
        // given
        User mentionerUser = UserFixtureBuilder.getUserWithId(1L);
        MentionedUserInfo userInfo = new MentionedUserInfo(2L, "홍길동");
        List<MentionedUserInfo> mentionedUserInfos = List.of(userInfo);

        // mock
        when(mentionRepository.existsByMentionerUser_IdAndMentionedUser_IdAndCreatedAtGreaterThanEqual(eq(1L), eq(2L), any(Instant.class)))
                .thenReturn(false);

        // when & then
        mentionWriter.validateMentionCooldown(mentionedUserInfos, mentionerUser);
    }

    @Test
    void validateMentionCooldown_쿨다운_위반_에러() {
        // given
        User mentionerUser = UserFixtureBuilder.getUserWithId(1L);
        MentionedUserInfo userInfo = new MentionedUserInfo(2L, "홍길동");
        List<MentionedUserInfo> mentionedUserInfos = List.of(userInfo);

        // mock
        when(mentionRepository.existsByMentionerUser_IdAndMentionedUser_IdAndCreatedAtGreaterThanEqual(eq(1L), eq(2L), any(Instant.class)))
                .thenReturn(true);

        // when & then
        assertThatThrownBy(() -> mentionWriter.validateMentionCooldown(mentionedUserInfos, mentionerUser))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(MentionErrorCode.MENTION_COOLDOWN_VIOLATION);
    }

    @Test
    void validateMentionCooldown_여러_사용자_쿨다운_검증() {
        // given
        User mentionerUser = UserFixtureBuilder.getUserWithId(1L);
        MentionedUserInfo userInfo1 = new MentionedUserInfo(2L, "홍길동");
        MentionedUserInfo userInfo2 = new MentionedUserInfo(3L, "김철수");
        List<MentionedUserInfo> mentionedUserInfos = List.of(userInfo1, userInfo2);

        // mock
        when(mentionRepository.existsByMentionerUser_IdAndMentionedUser_IdAndCreatedAtGreaterThanEqual(eq(1L), eq(2L), any(Instant.class)))
                .thenReturn(false);
        when(mentionRepository.existsByMentionerUser_IdAndMentionedUser_IdAndCreatedAtGreaterThanEqual(eq(1L), eq(3L), any(Instant.class)))
                .thenReturn(false);

        // when & then
        mentionWriter.validateMentionCooldown(mentionedUserInfos, mentionerUser);
    }
}
