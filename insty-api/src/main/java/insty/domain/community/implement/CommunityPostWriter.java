package insty.domain.community.implement;

import insty.domain.community.repository.CommunityPostRepository;
import insty.model.community.CommunityPost;
import insty.model.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional
public class CommunityPostWriter {

    private final CommunityPostRepository communityPostRepository;

    public CommunityPost savePost(User user, String title, String content) {
        CommunityPost post = CommunityPost.create(user, title, content);
        return communityPostRepository.save(post);
    }

    public CommunityPost updatePost(CommunityPost post, String title, String content) {
        post.update(title, content);
        return post;
    }

    public void deletePost(CommunityPost post) {
        post.markAsDeleted();
        communityPostRepository.save(post);
    }
}
