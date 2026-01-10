package insty.domain.community.repository;

import insty.model.community.CommunityComment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityCommentRepository extends JpaRepository<CommunityComment, Long> {

    List<CommunityComment> findAllByCommunityPost_IdAndIsDeletedFalse(Long postId);

    List<CommunityComment> findAllByCommunityPost_IdAndIsDeletedFalseOrderByCreatedAtDesc(Long postId);

    List<CommunityComment> findAllByCommunityPost_Id(Long postId);

    Page<CommunityComment> findAllByUser_IdAndIsDeletedFalse(Long userId, Pageable pageable);

    Optional<CommunityComment> findByIdAndIsDeletedFalse(Long commentId);
}
