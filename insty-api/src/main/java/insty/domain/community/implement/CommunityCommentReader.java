package insty.domain.community.implement;

import insty.domain.community.repository.CommunityCommentRepository;
import insty.error.CommunityErrorCode;
import insty.exception.CustomException;
import insty.model.community.CommunityComment;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityCommentReader {

    private final CommunityCommentRepository communityCommentRepository;

    public CommunityComment getComment(Long commentId) {
        return communityCommentRepository.findByIdAndIsDeletedFalse(commentId)
                .orElseThrow(() -> new CustomException(CommunityErrorCode.COMMUNITY_COMMENT_NOT_FOUND));
    }

    public List<CommunityComment> getCommentsByPostId(Long postId) {
        return communityCommentRepository.findAllByCommunityPost_IdAndIsDeletedFalseOrderByCreatedAtDesc(postId);
    }

    public Map<Long, Long> countByPostIds(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Map.of();
        }
        return communityCommentRepository.countByPostIds(postIds).stream()
                .collect(Collectors.toMap(
                        projection -> projection.getPostId(),
                        projection -> projection.getCount()
                ));
    }

    public long countByPostId(Long postId) {
        return communityCommentRepository.countByPostId(postId);
    }

    public Page<CommunityComment> getCommentsByUser(Long userId, Pageable pageable) {
        return communityCommentRepository.findAllByUser_IdAndIsDeletedFalse(userId, pageable);
    }
}
