package insty.domain.community.repository;

import insty.model.community.CommunityPostLike;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommunityPostLikeRepository extends JpaRepository<CommunityPostLike, Long> {

    boolean existsByCommunityPostIdAndUserId(Long postId, Long userId);

    void deleteByCommunityPostIdAndUserId(Long postId, Long userId);

    @Query("""
        SELECT l.communityPost.id
        FROM CommunityPostLike l
        WHERE l.user.id = :userId
          AND l.communityPost.id IN :postIds
    """)
    List<Long> findPostIdsByUserIdAndPostIdIn(@Param("userId") Long userId, @Param("postIds") List<Long> postIds);
}
