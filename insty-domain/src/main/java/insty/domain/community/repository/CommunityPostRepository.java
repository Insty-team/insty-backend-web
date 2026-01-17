package insty.domain.community.repository;

import insty.model.community.CommunityPost;
import java.util.Optional;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long> {

    Page<CommunityPost> findAllByCourse_IdAndIsDeletedFalse(Long courseId, Pageable pageable);

    Page<CommunityPost> findAllByUser_IdAndIsDeletedFalse(Long userId, Pageable pageable);

    @Query("""
        SELECT p FROM CommunityPost p
        WHERE p.user.id = :userId
          AND p.isDeleted = false
          AND (
              LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
              OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))
          )
    """)
    Page<CommunityPost> searchAllByUserIdAndKeyword(@Param("userId") Long userId, @Param("keyword") String keyword, Pageable pageable);

    List<CommunityPost> findAllByCourse_Id(Long courseId);

    Optional<CommunityPost> findByIdAndIsDeletedFalse(Long id);

    @Modifying
    @Query("""
        UPDATE CommunityPost p
        SET p.likeCount = p.likeCount + 1
        WHERE p.id = :postId
    """)
    int incrementLikeCount(@Param("postId") Long postId);

    @Modifying
    @Query("""
        UPDATE CommunityPost p
        SET p.likeCount = p.likeCount - 1
        WHERE p.id = :postId AND p.likeCount > 0
    """)
    int decrementLikeCount(@Param("postId") Long postId);

    @Query("""
        SELECT p.likeCount
        FROM CommunityPost p
        WHERE p.id = :postId
    """)
    int findLikeCountById(@Param("postId") Long postId);

    @Query("""
        SELECT p FROM CommunityPost p
        JOIN FETCH p.user u
        LEFT JOIN FETCH p.attachments att
        LEFT JOIN FETCH att.file f
        WHERE p.id = :postId AND p.isDeleted = false
    """)
    Optional<CommunityPost> findDetailsWithUserAndAttachments(@Param("postId") Long postId);
}
