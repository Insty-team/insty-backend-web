package insty.domain.video.repository;

import insty.model.video.VideoCommunityComment;
import insty.model.video.EncodingStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VideoCommunityCommentRepository extends JpaRepository<VideoCommunityComment, Long> {

    Optional<VideoCommunityComment> findByVideoUuid(UUID videoUuid);

    Optional<VideoCommunityComment> findByCommunityCommentIdAndIsDeleted(Long commentId, boolean isDeleted);

    @Query("SELECT vc.videoUuid FROM VideoCommunityComment vc WHERE vc.communityComment.id = :commentId AND vc.isDeleted = false")
    Optional<UUID> findVideoUuidByCommunityCommentId(@Param("commentId") Long commentId);

    @Query("SELECT vc.duration FROM VideoCommunityComment vc "
            + "WHERE vc.user.id = :userId AND vc.encodingAt >= :encodingAt AND vc.encodingStatus in :encodingStatus")
    List<Integer> findEncodingDuration(@Param("userId") Long userId, @Param("encodingAt") Instant encodingAt,
                                       @Param("encodingStatus") List<EncodingStatus> encodingStatuses);

    List<VideoCommunityComment> findAllByIsDeletedTrue(Pageable pageable);
}
