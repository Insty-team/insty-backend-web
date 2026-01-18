package insty.domain.community.repository;

import insty.model.community.CommunityComment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommunityCommentRepository extends JpaRepository<CommunityComment, Long> {

    List<CommunityComment> findAllByCommunityPost_IdAndIsDeletedFalse(Long postId);

    List<CommunityComment> findAllByCommunityPost_IdAndIsDeletedFalseOrderByCreatedAtDesc(Long postId);

    List<CommunityComment> findAllByCommunityPost_Id(Long postId);

    Page<CommunityComment> findAllByUser_IdAndIsDeletedFalse(Long userId, Pageable pageable);

    Optional<CommunityComment> findByIdAndIsDeletedFalse(Long commentId);

    @Query("""
        SELECT c.communityPost.id AS postId, COUNT(c) AS count
        FROM CommunityComment c
        WHERE c.communityPost.id IN :postIds AND c.isDeleted = false
        GROUP BY c.communityPost.id
    """)
    List<CommunityCommentCountProjection> countByPostIds(@Param("postIds") List<Long> postIds);

    @Query("""
        SELECT COUNT(c)
        FROM CommunityComment c
        WHERE c.communityPost.id = :postId AND c.isDeleted = false
    """)
    long countByPostId(@Param("postId") Long postId);

    @Modifying
    @Query("""
        UPDATE CommunityComment c
        SET c.likeCount = c.likeCount + 1
        WHERE c.id = :commentId
    """)
    int incrementLikeCount(@Param("commentId") Long commentId);

    @Modifying
    @Query("""
        UPDATE CommunityComment c
        SET c.likeCount = c.likeCount - 1
        WHERE c.id = :commentId AND c.likeCount > 0
    """)
    int decrementLikeCount(@Param("commentId") Long commentId);

    @Query("""
        SELECT c.likeCount
        FROM CommunityComment c
        WHERE c.id = :commentId
    """)
    int findLikeCountById(@Param("commentId") Long commentId);
}
