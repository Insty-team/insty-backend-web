package insty.domain.community.implement;

import insty.domain.community.dto.CommunityLikeRes;
import insty.domain.community.repository.CommunityCommentLikeRepository;
import insty.domain.community.repository.CommunityCommentRepository;
import insty.model.community.CommunityComment;
import insty.model.community.CommunityCommentLike;
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
public class CommunityCommentLikeManager {

    private final CommunityCommentLikeRepository communityCommentLikeRepository;
    private final CommunityCommentRepository communityCommentRepository;

    public Set<Long> getLikedCommentIds(Long userId, List<Long> commentIds) {
        if (userId == null || commentIds == null || commentIds.isEmpty()) {
            return Collections.emptySet();
        }
        return new HashSet<>(communityCommentLikeRepository.findCommentIdsByUserIdAndCommentIdIn(userId, commentIds));
    }

    public boolean isLikedByUser(Long userId, Long commentId) {
        if (userId == null || commentId == null) {
            return false;
        }
        return communityCommentLikeRepository.existsByCommunityCommentIdAndUserId(commentId, userId);
    }

    public CommunityLikeRes likeComment(CommunityComment comment, User user) {
        Long commentId = comment.getId();
        Long userId = user.getId();
        if (communityCommentLikeRepository.existsByCommunityCommentIdAndUserId(commentId, userId)) {
            int count = communityCommentRepository.findLikeCountById(commentId);
            return new CommunityLikeRes(count, true);
        }
        communityCommentLikeRepository.save(CommunityCommentLike.create(comment, user));
        communityCommentRepository.incrementLikeCount(commentId);
        int count = communityCommentRepository.findLikeCountById(commentId);
        return new CommunityLikeRes(count, true);
    }

    public CommunityLikeRes unlikeComment(CommunityComment comment, User user) {
        Long commentId = comment.getId();
        Long userId = user.getId();
        if (!communityCommentLikeRepository.existsByCommunityCommentIdAndUserId(commentId, userId)) {
            int count = communityCommentRepository.findLikeCountById(commentId);
            return new CommunityLikeRes(count, false);
        }
        communityCommentLikeRepository.deleteByCommunityCommentIdAndUserId(commentId, userId);
        communityCommentRepository.decrementLikeCount(commentId);
        int count = communityCommentRepository.findLikeCountById(commentId);
        return new CommunityLikeRes(count, false);
    }
}
