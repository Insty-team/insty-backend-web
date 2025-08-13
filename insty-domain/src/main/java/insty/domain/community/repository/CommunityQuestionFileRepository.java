package insty.domain.community.repository;

import insty.model.community.CommunityQuestionFile;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface CommunityQuestionFileRepository extends JpaRepository<CommunityQuestionFile, Long> {

    List<CommunityQuestionFile> findAllByCommunityQuestionId(Long questionId);

    @Modifying
    @Transactional
    @Query("""
        DELETE FROM CommunityQuestionFile cf
        WHERE cf.communityQuestion.id = :questionId
          AND cf.file.id IN :fileIds
    """)
    void deleteByQuestionIdAndFileIdIn(Long questionId, List<Long> fileIds);

    int countByCommunityQuestionId(Long questionId);

    @Modifying
    @Transactional
    @Query("""
        DELETE FROM CommunityQuestionFile cf
        WHERE cf.communityQuestion.id = :questionId
    """)
    void deleteAllByQuestionId(Long questionId);
}
