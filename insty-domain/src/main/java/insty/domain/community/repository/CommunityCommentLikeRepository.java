package insty.domain.community.repository;

import insty.model.community.CommunityCommentLike;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommunityCommentLikeRepository extends JpaRepository<CommunityCommentLike, Long> {

    boolean existsByCommunityCommentIdAndUserId(Long commentId, Long userId);

    void deleteByCommunityCommentIdAndUserId(Long commentId, Long userId);

    @Query("""
        SELECT l.communityComment.id
        FROM CommunityCommentLike l
        WHERE l.user.id = :userId
          AND l.communityComment.id IN :commentIds
    """)
    List<Long> findCommentIdsByUserIdAndCommentIdIn(@Param("userId") Long userId, @Param("commentIds") List<Long> commentIds);
}
