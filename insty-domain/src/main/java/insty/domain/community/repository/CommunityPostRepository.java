package insty.domain.community.repository;

import insty.model.community.CommunityPost;
import java.util.Optional;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long> {

    Page<CommunityPost> findAllByCourse_IdAndIsDeletedFalse(Long courseId, Pageable pageable);

    Page<CommunityPost> findAllByUser_IdAndIsDeletedFalse(Long userId, Pageable pageable);
    
    List<CommunityPost> findAllByCourse_Id(Long courseId);

    Optional<CommunityPost> findByIdAndIsDeletedFalse(Long id);

    @Query("""
        SELECT p FROM CommunityPost p
        JOIN FETCH p.user u
        LEFT JOIN FETCH p.attachments att
        LEFT JOIN FETCH att.file f
        WHERE p.id = :postId AND p.isDeleted = false
    """)
    Optional<CommunityPost> findDetailsWithUserAndAttachments(@Param("postId") Long postId);
}
