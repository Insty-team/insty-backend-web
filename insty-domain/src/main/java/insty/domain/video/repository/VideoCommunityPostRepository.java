package insty.domain.video.repository;

import insty.model.video.VideoCommunityPost;
import insty.model.video.EncodingStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VideoCommunityPostRepository extends JpaRepository<VideoCommunityPost, Long> {

    Optional<VideoCommunityPost> findByVideoUuid(UUID videoUuid);

    Optional<VideoCommunityPost> findByCommunityPostIdAndIsDeleted(Long postId, boolean isDeleted);

    @Query("SELECT vp.videoUuid FROM VideoCommunityPost vp WHERE vp.communityPost.id = :postId AND vp.isDeleted = false")
    Optional<UUID> findVideoUuidByCommunityPostId(@Param("postId") Long postId);

    List<VideoCommunityPost> findAllByCommunityPostIdInAndIsDeletedFalse(List<Long> postIds);

    @Query("SELECT vp.duration FROM VideoCommunityPost vp "
            + "WHERE vp.user.id = :userId AND vp.encodingAt >= :encodingAt AND vp.encodingStatus in :encodingStatus")
    List<Integer> findEncodingDuration(@Param("userId") Long userId, @Param("encodingAt") Instant encodingAt,
                                       @Param("encodingStatus") List<EncodingStatus> encodingStatuses);

    List<VideoCommunityPost> findAllByIsDeletedTrue(Pageable pageable);
}
