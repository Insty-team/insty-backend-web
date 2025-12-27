package insty.domain.community.implement;

import insty.domain.community.repository.CommunityCommentRepository;
import insty.model.community.CommunityComment;
import insty.model.community.CommunityPost;
import insty.model.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional
public class CommunityCommentWriter {

    private final CommunityCommentRepository communityCommentRepository;

    public CommunityComment saveComment(CommunityPost post, User user, String content) {
        CommunityComment comment = CommunityComment.create(post, user, content);
        return communityCommentRepository.save(comment);
    }

    public CommunityComment updateComment(CommunityComment comment, String content) {
        comment.update(content);
        return comment;
    }

    public void deleteComment(CommunityComment comment) {
        comment.markAsDeleted();
        communityCommentRepository.save(comment);
    }
}
