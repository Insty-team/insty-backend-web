package insty.domain.mention.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import insty.domain.mention.dto.MentionedUserInfo;
import insty.error.MentionErrorCode;
import insty.exception.CustomException;
import insty.model.user.User;
import insty.model.user.UserFixtureBuilder;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class MentionParserTest {

    @InjectMocks
    private MentionParser mentionParser;

    @Test
    void parseMentionedUserInfos_정상() {
        // given
        User mentionerUser = UserFixtureBuilder.getUserWithId(1L);
        String content = "안녕하세요 @[홍길동](2)님과 @[김철수](3)님!";

        // when
        List<MentionedUserInfo> result = mentionParser.parseMentionedUserInfos(content, mentionerUser);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).userId()).isEqualTo(2L);
        assertThat(result.get(0).displayName()).isEqualTo("홍길동");
        assertThat(result.get(1).userId()).isEqualTo(3L);
        assertThat(result.get(1).displayName()).isEqualTo("김철수");
    }

    @Test
    void parseMentionedUserInfos_중복_멘션_제거() {
        // given
        User mentionerUser = UserFixtureBuilder.getUserWithId(1L);
        String content = "안녕하세요 @[홍길동](2)님과 @[홍길동](2)님!";

        // when
        List<MentionedUserInfo> result = mentionParser.parseMentionedUserInfos(content, mentionerUser);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).userId()).isEqualTo(2L);
        assertThat(result.get(0).displayName()).isEqualTo("홍길동");
    }

    @Test
    void parseMentionedUserInfos_멘션_없음() {
        // given
        User mentionerUser = UserFixtureBuilder.getUserWithId(1L);
        String content = "안녕하세요! 멘션 없습니다.";

        // when
        List<MentionedUserInfo> result = mentionParser.parseMentionedUserInfos(content, mentionerUser);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void parseMentionedUserInfos_자기_자신_멘션_에러() {
        // given
        User mentionerUser = UserFixtureBuilder.getUserWithId(1L);
        String content = "안녕하세요 @[나](1)님!";

        // when & then
        assertThatThrownBy(() -> mentionParser.parseMentionedUserInfos(content, mentionerUser))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(MentionErrorCode.MENTION_SELF_ERROR);
    }

    @Test
    void parseMentionedUserInfos_멘션_제한_초과_에러() {
        // given
        User mentionerUser = UserFixtureBuilder.getUserWithId(1L);
        String content = "안녕하세요 @[홍길동](2)님과 @[김철수](3)님과 @[박영희](4)님!";

        // when & then
        assertThatThrownBy(() -> mentionParser.parseMentionedUserInfos(content, mentionerUser))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(MentionErrorCode.MENTION_LIMIT_EXCEEDED);
    }

    @Test
    void parseMentionedUserInfos_잘못된_사용자ID_형식_에러() {
        // given
        User mentionerUser = UserFixtureBuilder.getUserWithId(1L);
        String content = "안녕하세요 @[홍길동](2)님과 @[김철수](12a)님!";

        // when & then
        assertThatThrownBy(() -> mentionParser.parseMentionedUserInfos(content, mentionerUser))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(MentionErrorCode.MENTION_INVALID_FORMAT);
    }

    @Test
    void parseMentionedUserInfos_복잡한_멘션_형식() {
        // given
        User mentionerUser = UserFixtureBuilder.getUserWithId(1L);
        String content = "안녕하세요 @[홍길동(개발자)](2)님과 @[김철수-프론트엔드](3)님!";

        // when
        List<MentionedUserInfo> result = mentionParser.parseMentionedUserInfos(content, mentionerUser);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).userId()).isEqualTo(2L);
        assertThat(result.get(0).displayName()).isEqualTo("홍길동(개발자)");
        assertThat(result.get(1).userId()).isEqualTo(3L);
        assertThat(result.get(1).displayName()).isEqualTo("김철수-프론트엔드");
    }
}
