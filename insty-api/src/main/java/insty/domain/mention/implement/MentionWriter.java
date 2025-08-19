package insty.domain.mention.implement;

import insty.domain.mention.repository.MentionRepository;
import insty.domain.user.repository.UserRepository;
import insty.error.MentionErrorCode;
import insty.exception.CustomException;
import insty.model.community.CommunityAnswer;
import insty.model.mention.Mention;
import insty.model.user.User;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MentionWriter {

    private final MentionRepository mentionRepository;
    private final UserRepository userRepository;
    
    private static final Pattern MENTION_PATTERN = Pattern.compile("@\\[([^\\]]+)\\]\\((\\d+)\\)");
    private static final int MAX_MENTIONS_PER_COMMENT = 2;
    private static final int MENTION_COOLDOWN_MINUTES = 5;

    /**
     * 댓글에서 멘션 파싱 및 저장
     */
    public List<Mention> parseAndSaveMentions(CommunityAnswer communityAnswer, User mentionerUser, String content) {
        List<MentionedUserInfo> mentionedUsers = parseMentionedUsers(content);
        
        if (mentionedUsers.size() > MAX_MENTIONS_PER_COMMENT) {
            throw new CustomException(MentionErrorCode.MENTION_LIMIT_EXCEEDED);
        }
        
        validateMentionCooldown(mentionerUser, mentionedUsers);
        
        List<Mention> mentions = new ArrayList<>();
        Set<Long> processedUserIds = new HashSet<>();
        
        for (MentionedUserInfo userInfo : mentionedUsers) {
            if (processedUserIds.contains(userInfo.userId)) {
                continue;
            }
            
            Optional<User> mentionedUserOpt = userRepository.findById(userInfo.userId);
            
            if (mentionedUserOpt.isPresent()) {
                User mentionedUser = mentionedUserOpt.get();
                
                if (mentionedUser.getId().equals(mentionerUser.getId())) {
                    throw new CustomException(MentionErrorCode.MENTION_SELF_ERROR);
                }
                
                Mention mention = Mention.create(communityAnswer, mentionedUser, mentionerUser);
                mentions.add(mentionRepository.save(mention));
                processedUserIds.add(userInfo.userId);
            }
        }
        
        return mentions;
    }

    /**
     * 멘션 형식에서 사용자 정보 추출
     */
    private List<MentionedUserInfo> parseMentionedUsers(String content) {
        List<MentionedUserInfo> mentionedUsers = new ArrayList<>();
        
        // @[표시명](user_id) 형식 파싱
        Matcher matcher = MENTION_PATTERN.matcher(content);
        while (matcher.find()) {
            String displayName = matcher.group(1);
            String userIdStr = matcher.group(2);
            try {
                Long userId = Long.parseLong(userIdStr);
                mentionedUsers.add(new MentionedUserInfo(userId, displayName));
            } catch (NumberFormatException e) {
                //TODO: 잘못된 형식 처리 필요
                log.warn("잘못된 사용자 ID 형식: {}", userIdStr);
            }
        }
        
        return mentionedUsers;
    }

    /**
     * 멘션 쿨다운 검증
     */
    private void validateMentionCooldown(User mentionerUser, List<MentionedUserInfo> mentionedUsers) {
        LocalDateTime cooldownThreshold = LocalDateTime.now().minusMinutes(MENTION_COOLDOWN_MINUTES);
        
        for (MentionedUserInfo userInfo : mentionedUsers) {
            List<Mention> recentMentions = mentionRepository.findRecentMentionsByMentionerAndMentioned(
                mentionerUser.getId(), userInfo.userId, cooldownThreshold);
            
            if (!recentMentions.isEmpty()) {
                log.warn("멘션 쿨다운 위반: mentioner={}, mentioned={}", mentionerUser.getId(), userInfo.userId);
                throw new CustomException(MentionErrorCode.MENTION_COOLDOWN_VIOLATION);
            }
        }
    }

    /**
     * 멘션된 사용자 정보
     */
    private static class MentionedUserInfo {
        final Long userId;
        final String displayName;
        
        MentionedUserInfo(Long userId, String displayName) {
            this.userId = userId;
            this.displayName = displayName;
        }
    }
}
