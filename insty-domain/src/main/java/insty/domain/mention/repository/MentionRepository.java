package insty.domain.mention.repository;

import insty.model.mention.Mention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface MentionRepository extends JpaRepository<Mention, Long> {

    @Query("SELECT m FROM Mention m WHERE m.communityAnswer.id = :answerId")
    List<Mention> findAllByCommunityAnswerId(@Param("answerId") Long answerId);

    @Query("SELECT m FROM Mention m WHERE m.mentionedUser.id = :userId")
    List<Mention> findAllByMentionedUserId(@Param("userId") Long userId);

    @Query("SELECT m FROM Mention m WHERE m.mentionerUser.id = :userId")
    List<Mention> findAllByMentionerUserId(@Param("userId") Long userId);

    @Query("SELECT m FROM Mention m WHERE m.mentionerUser.id = :mentionerId AND m.mentionedUser.id = :mentionedId AND m.createdAt >= :since")
    List<Mention> findRecentMentionsByMentionerAndMentioned(
        @Param("mentionerId") Long mentionerId, 
        @Param("mentionedId") Long mentionedId, 
        @Param("since") Instant since
    );

    boolean existsByMentionerUser_IdAndMentionedUser_IdAndCreatedAtGreaterThanEqual(
        Long mentionerId, Long mentionedId, Instant since);
}
