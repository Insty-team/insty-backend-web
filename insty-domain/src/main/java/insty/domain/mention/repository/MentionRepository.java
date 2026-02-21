package insty.domain.mention.repository;

import insty.model.mention.Mention;
import insty.model.mention.MentionTargetType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MentionRepository extends JpaRepository<Mention, Long> {

    List<Mention> findAllByTargetTypeAndTargetId(MentionTargetType targetType, Long targetId);

    Optional<Mention> findByTargetTypeAndTargetIdAndMentionedUser_IdAndMentionerUser_Id(
        MentionTargetType targetType, Long targetId, Long mentionedUserId, Long mentionerUserId);

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
