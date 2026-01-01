package insty.domain.community.implement;

import insty.domain.community.repository.CommunityCommentRepository;
import insty.domain.community.repository.CommunityPostRepository;
import insty.model.community.CommunityComment;
import insty.model.community.CommunityPost;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@RequiredArgsConstructor
public class CommunityCourseCleaner {

    private final CommunityPostRepository communityPostRepository;
    private final CommunityCommentRepository communityCommentRepository;
    private final CommunityPostFileWriter communityPostFileWriter;
    private final CommunityCommentFileWriter communityCommentFileWriter;
    private final CommunityPostVideoManager communityPostVideoManager;
    private final CommunityCommentVideoManager communityCommentVideoManager;

    /**
     * 강좌 삭제 시 커뮤니티 게시글/댓글과 첨부/영상까지 모두 정리한다.
     */
    public void deleteAllByCourseId(Long courseId) {
        List<CommunityPost> posts = communityPostRepository.findAllByCourse_Id(courseId);
        for (CommunityPost post : posts) {
            deleteComments(post);
            communityPostFileWriter.deletePostFiles(post);
            communityPostVideoManager.deleteVideo(post);
            communityPostRepository.delete(post);
        }
    }

    private void deleteComments(CommunityPost post) {
        List<CommunityComment> comments = communityCommentRepository.findAllByCommunityPost_IdAndIsDeletedFalse(post.getId());
        for (CommunityComment comment : comments) {
            communityCommentFileWriter.deleteCommentFiles(comment);
            communityCommentVideoManager.deleteVideo(comment);
            communityCommentRepository.delete(comment);
        }
    }
}
