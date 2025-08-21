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
import java.util.Optional;
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

        for (MentionedUserInfo userInfo : mentionedUserInfos) {
            Optional<User> mentionedUserOpt = userRepository.findById(userInfo.userId());

            if (mentionedUserOpt.isEmpty()) {
                throw new CustomException(MentionErrorCode.MENTION_USER_NOT_FOUND);
            }

            Mention mention = Mention.create(communityAnswer, mentionedUserOpt.get(), mentionerUser);
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
            List<Mention> recentMentions = mentionRepository.findRecentMentionsByMentionerAndMentioned(
                    mentionerUser.getId(), userInfo.userId(), cooldownThreshold);

            if (!recentMentions.isEmpty()) {
                throw new CustomException(MentionErrorCode.MENTION_COOLDOWN_VIOLATION);
            }
        }
    }
}
