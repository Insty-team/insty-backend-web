package insty.domain.community.repository;

import insty.model.community.CommunityComment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityCommentRepository extends JpaRepository<CommunityComment, Long> {

    List<CommunityComment> findAllByCommunityPost_IdAndIsDeletedFalse(Long postId);

    Optional<CommunityComment> findByIdAndIsDeletedFalse(Long commentId);
}
