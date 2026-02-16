package insty.domain.video.repository;

import insty.model.video.VideoCommunityPost;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoCommunityPostRepository extends JpaRepository<VideoCommunityPost, Long> {

    Optional<VideoCommunityPost> findByVideoUuid(UUID videoUuid);

    Optional<VideoCommunityPost> findByCommunityPostIdAndIsDeleted(Long postId, boolean isDeleted);

    List<VideoCommunityPost> findAllByCommunityPostIdInAndIsDeletedFalse(List<Long> postIds);

    List<VideoCommunityPost> findAllByIsDeletedTrue(Pageable pageable);
}
