package insty.domain.community.repository;

import insty.model.community.CommunityAnswerFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface CommunityAnswerFileRepository extends JpaRepository<CommunityAnswerFile, Long> {

    List<CommunityAnswerFile> findAllByCommunityAnswerId(Long communityAnswerId);

    @Modifying
    @Transactional
    @Query("""
        DELETE FROM CommunityAnswerFile caf
        WHERE caf.communityAnswer.id = :answerId
          AND caf.file.id IN :fileIds
    """)
    void deleteByAnswerIdAndFileIdIn(Long answerId, List<Long> fileIds);

    int countByCommunityAnswerId(Long communityAnswerId);
}
