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
     * 멘션 가능한 사용자 목록을 검색
     */
    public List<User> searchMentionableUsers(int size, String searchKeyword, Long excludedUserId) {
        Pageable pageable = PageRequest.of(0, size);
        return userRepository.searchUsersByKeyword(searchKeyword, excludedUserId, pageable);
    }

    /**
     * 특정 댓글의 멘션 목록을 조회
     */
    public List<Mention> getMentionsByAnswerId(Long answerId) {
        return mentionRepository.findAllByCommunityAnswerId(answerId);
    }
}
