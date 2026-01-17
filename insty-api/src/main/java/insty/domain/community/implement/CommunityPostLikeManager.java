package insty.domain.community.implement;

import insty.domain.community.dto.CommunityLikeRes;
import insty.domain.community.repository.CommunityPostLikeRepository;
import insty.domain.community.repository.CommunityPostRepository;
import insty.model.community.CommunityPost;
import insty.model.community.CommunityPostLike;
import insty.model.user.User;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional
public class CommunityPostLikeManager {

    private final CommunityPostLikeRepository communityPostLikeRepository;
    private final CommunityPostRepository communityPostRepository;

    public Set<Long> getLikedPostIds(Long userId, List<Long> postIds) {
        if (userId == null || postIds == null || postIds.isEmpty()) {
            return Collections.emptySet();
        }
        return new HashSet<>(communityPostLikeRepository.findPostIdsByUserIdAndPostIdIn(userId, postIds));
    }

    public boolean isLikedByUser(Long userId, Long postId) {
        if (userId == null || postId == null) {
            return false;
        }
        return communityPostLikeRepository.existsByCommunityPostIdAndUserId(postId, userId);
    }

    public CommunityLikeRes likePost(CommunityPost post, User user) {
        Long postId = post.getId();
        Long userId = user.getId();
        if (communityPostLikeRepository.existsByCommunityPostIdAndUserId(postId, userId)) {
            int count = communityPostRepository.findLikeCountById(postId);
            return new CommunityLikeRes(count, true);
        }
        communityPostLikeRepository.save(CommunityPostLike.create(post, user));
        communityPostRepository.incrementLikeCount(postId);
        int count = communityPostRepository.findLikeCountById(postId);
        return new CommunityLikeRes(count, true);
    }

    public CommunityLikeRes unlikePost(CommunityPost post, User user) {
        Long postId = post.getId();
        Long userId = user.getId();
        if (!communityPostLikeRepository.existsByCommunityPostIdAndUserId(postId, userId)) {
            int count = communityPostRepository.findLikeCountById(postId);
            return new CommunityLikeRes(count, false);
        }
        communityPostLikeRepository.deleteByCommunityPostIdAndUserId(postId, userId);
        communityPostRepository.decrementLikeCount(postId);
        int count = communityPostRepository.findLikeCountById(postId);
        return new CommunityLikeRes(count, false);
    }
}
