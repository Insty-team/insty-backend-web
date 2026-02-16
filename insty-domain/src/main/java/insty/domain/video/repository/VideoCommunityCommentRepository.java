package insty.domain.video.repository;

import insty.model.video.VideoCommunityComment;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoCommunityCommentRepository extends JpaRepository<VideoCommunityComment, Long> {

    Optional<VideoCommunityComment> findByVideoUuid(UUID videoUuid);

    Optional<VideoCommunityComment> findByCommunityCommentIdAndIsDeleted(Long commentId, boolean isDeleted);

    List<VideoCommunityComment> findAllByIsDeletedTrue();
}
