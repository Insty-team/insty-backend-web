package insty.domain.mention.implement;

import insty.domain.mention.repository.MentionRepository;
import insty.domain.user.repository.UserRepository;
import insty.model.mention.Mention;
import insty.model.user.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MentionReader {

    private final UserRepository userRepository;
    private final MentionRepository mentionRepository;

    /**
     * 멘션 가능한 사용자 목록을 검색한다
     */
    public List<User> searchMentionableUsers(int size, String searchKeyword, Long excludedUserId) {
        Pageable pageable = PageRequest.of(0, size);

        List<User> users = userRepository.searchUsersByKeyword(
                searchKeyword,
                excludedUserId,
                pageable
        );
        return users;
    }

    /**
     * 특정 댓글의 멘션 목록을 조회한다
     */
    public List<Mention> getMentionsByAnswerId(Long answerId) {
        log.debug("댓글 멘션 목록 조회: answerId={}", answerId);
        return mentionRepository.findAllByCommunityAnswerId(answerId);
    }
}
