package insty.domain.mention.implement;

import insty.domain.mention.dto.MentionedUserInfo;
import insty.domain.mention.repository.MentionRepository;
import insty.domain.user.repository.UserRepository;
import insty.error.MentionErrorCode;
import insty.exception.CustomException;
import insty.model.community.CommunityAnswer;
import insty.model.mention.Mention;
import insty.model.user.User;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    private static final int MENTION_COOLDOWN_MINUTES = 5;

    /**
     * 멘션된 사용자 정보 리스트를 받아서 Mention 객체를 생성하고 저장
     */
    public List<Mention> saveMentions(List<MentionedUserInfo> mentionedUserInfos, User mentionerUser,
                                      CommunityAnswer communityAnswer) {
        List<Mention> savedMentions = new ArrayList<>();
        if (mentionedUserInfos == null || mentionedUserInfos.isEmpty()) {
            return savedMentions;
        }

        Set<Long> ids = mentionedUserInfos.stream()
                .map(MentionedUserInfo::userId)
                .collect(java.util.stream.Collectors.toSet());
        Map<Long, User> usersById = userRepository.findAllById(ids).stream()
                .collect(java.util.stream.Collectors.toMap(User::getId, u -> u));

        if (usersById.size() != ids.size()) {
            throw new CustomException(MentionErrorCode.MENTION_USER_NOT_FOUND);
        }

        for (MentionedUserInfo userInfo : mentionedUserInfos) {
            User mentionedUser = usersById.get(userInfo.userId());
            Mention mention = Mention.create(communityAnswer, mentionedUser, mentionerUser);
            savedMentions.add(mentionRepository.save(mention));
        }
        return savedMentions;
    }

    /**
     * 멘션 쿨다운 검증
     */
    public void validateMentionCooldown(List<MentionedUserInfo> mentionedUserInfos, User mentionerUser) {
        Instant cooldownThreshold = Instant.now().minusSeconds(MENTION_COOLDOWN_MINUTES * 60L);

        for (MentionedUserInfo userInfo : mentionedUserInfos) {
            boolean exists = mentionRepository
                    .existsByMentionerUser_IdAndMentionedUser_IdAndCreatedAtGreaterThanEqual(
                            mentionerUser.getId(), userInfo.userId(), cooldownThreshold);
            if (exists) {
                throw new CustomException(MentionErrorCode.MENTION_COOLDOWN_VIOLATION);
            }
        }
    }
}
