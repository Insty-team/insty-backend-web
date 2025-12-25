package insty.domain.mention.repository;

import insty.model.mention.Mention;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MentionRepository extends JpaRepository<Mention, Long> {

    @Query("SELECT m FROM Mention m WHERE m.courseAnswer.id = :answerId")
    List<Mention> findAllByCommunityAnswerId(@Param("answerId") Long answerId);

    boolean existsByMentionerUser_IdAndMentionedUser_IdAndCreatedAtGreaterThanEqual(
        Long mentionerId, Long mentionedId, Instant since);

    @Query("""
            select distinct m.mentionedUser.id
            from Mention m
            where m.mentionerUser.id = :mentionerId
              and m.mentionedUser.id in :mentionedIds
              and m.createdAt >= :since
            """)
    List<Long> findRecentlyMentionedUserIds(@Param("mentionerId") Long mentionerId,
                                            @Param("mentionedIds") java.util.Collection<Long> mentionedIds,
                                            @Param("since") Instant since);
}
