package insty.domain.community.implement;

import insty.domain.community.repository.CommunityPostCountProjection;
import insty.domain.community.repository.CommunityPostRepository;
import insty.error.CommunityErrorCode;
import insty.exception.CustomException;
import insty.model.community.CommunityPost;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import org.springframework.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityPostReader {

    private final CommunityPostRepository communityPostRepository;

    public CommunityPost getPost(Long postId) {
        return communityPostRepository.findByIdAndIsDeletedFalse(postId)
                .orElseThrow(() -> new CustomException(CommunityErrorCode.COMMUNITY_POST_NOT_FOUND));
    }

    public CommunityPost getPostWithAttachments(Long postId) {
        Optional<CommunityPost> post = communityPostRepository.findDetailsWithUserAndAttachments(postId);
        return post.filter(p -> !p.isDeleted())
                .orElseThrow(() -> new CustomException(CommunityErrorCode.COMMUNITY_POST_NOT_FOUND));
    }

    public Page<CommunityPost> findPosts(Long courseId, Pageable pageable) {
        return communityPostRepository.findAllByCourse_IdAndIsDeletedFalse(courseId, pageable);
    }

    public Page<CommunityPost> findPostsByUser(Long userId, String keyword, Pageable pageable) {
        if (!StringUtils.hasText(keyword)) {
            return communityPostRepository.findAllByUser_IdAndIsDeletedFalse(userId, pageable);
        }
        return communityPostRepository.searchAllByUserIdAndKeyword(userId, keyword, pageable);
    }

    public Map<Long, Long> getCountByCourseIds(List<Long> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) {
            return Map.of();
        }
        return communityPostRepository.countByCourseIds(courseIds).stream()
                .collect(Collectors.toMap(CommunityPostCountProjection::getCourseId, CommunityPostCountProjection::getCount));
    }

}
