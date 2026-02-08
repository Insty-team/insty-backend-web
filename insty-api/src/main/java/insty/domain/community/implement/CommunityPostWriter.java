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
        String safeTitle = (title == null || title.isBlank()) ? "unused-title" : title;
        CommunityPost post = CommunityPost.create(user, course, safeTitle, content);
        return communityPostRepository.save(post);
    }

    public CommunityPost updatePost(CommunityPost post, String title, String content) {
        String safeTitle = (title == null || title.isBlank()) ? "unused-title" : title;
        post.update(safeTitle, content);
        return post;
    }

    public void deletePost(CommunityPost post) {
        post.markAsDeleted();
        communityPostRepository.save(post);
    }
}
