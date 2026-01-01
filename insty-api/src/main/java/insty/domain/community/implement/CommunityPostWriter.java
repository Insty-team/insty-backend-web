package insty.domain.community.implement;

import insty.domain.community.repository.CommunityPostRepository;
import insty.model.community.CommunityPost;
import insty.model.course.Course;
import insty.model.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional
public class CommunityPostWriter {

    private final CommunityPostRepository communityPostRepository;

    public CommunityPost savePost(User user, Course course, String title, String content) {
        CommunityPost post = CommunityPost.create(user, course, title, content);
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
