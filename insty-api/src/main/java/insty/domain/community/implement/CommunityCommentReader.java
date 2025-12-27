package insty.domain.community.implement;

import insty.domain.community.repository.CommunityCommentRepository;
import insty.error.CommunityErrorCode;
import insty.exception.CustomException;
import insty.model.community.CommunityComment;
import java.util.List;
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
        return communityCommentRepository.findAllByCommunityPost_IdAndIsDeletedFalse(postId);
    }
}
