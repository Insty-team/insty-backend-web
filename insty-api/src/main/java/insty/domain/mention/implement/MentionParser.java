package insty.domain.mention.implement;

import insty.domain.mention.dto.MentionedUserInfo;
import insty.error.MentionErrorCode;
import insty.exception.CustomException;
import insty.model.user.User;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MentionParser {

    private static final Pattern MENTION_PATTERN = Pattern.compile("@\\[([^\\]]+)\\]\\(([^)]+)\\)");
    private static final int MAX_MENTIONS_PER_COMMENT = 2;

    /**
     * 콘텐츠에서 멘션된 사용자 정보를 파싱하여 반환
     */
    public List<MentionedUserInfo> parseMentionedUserInfos(String content, User mentionerUser) {
        List<MentionedUserInfo> parsed = parseMentionedUsers(content);
        // 사용자 ID 기준 중복 제거(입력 순서 보존)
        List<MentionedUserInfo> deduped = parsed.stream()
                .collect(java.util.stream.Collectors.collectingAndThen(
                        java.util.stream.Collectors.toMap(
                                MentionedUserInfo::userId,
                                java.util.function.Function.identity(),
                                (a, b) -> a,
                                java.util.LinkedHashMap::new
                        ),
                        m -> new java.util.ArrayList<>(m.values())
                ));

        if (deduped.size() > MAX_MENTIONS_PER_COMMENT) {
            throw new CustomException(MentionErrorCode.MENTION_LIMIT_EXCEEDED);
        }
        if (mentionerUser != null && mentionerUser.getId() != null &&
                deduped.stream().anyMatch(u -> u.userId().equals(mentionerUser.getId()))) {
            throw new CustomException(MentionErrorCode.MENTION_SELF_ERROR);
        }
        return java.util.List.copyOf(deduped);
    }

    /**
     * 멘션 형식에서 사용자 정보 추출
     */
    private List<MentionedUserInfo> parseMentionedUsers(String content) {
        List<MentionedUserInfo> mentionedUsers = new ArrayList<>();
        if (content == null || content.isBlank()) {
            return mentionedUsers;
        }
        Matcher matcher = MENTION_PATTERN.matcher(content);
        while (matcher.find()) {
            String displayName = matcher.group(1);
            String userIdStr = matcher.group(2);
            try {
                Long userId = Long.parseLong(userIdStr);
                mentionedUsers.add(new MentionedUserInfo(userId, displayName));
            } catch (NumberFormatException e) {
                throw new CustomException(MentionErrorCode.MENTION_INVALID_FORMAT);
            }
        }
        return mentionedUsers;
    }
}
