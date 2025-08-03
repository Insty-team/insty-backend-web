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
    @Query("delete from CommunityQuestionFile cf where cf.communityQuestion.id = :questionId and cf.file.id in :fileIds")
    void deleteByQuestionIdAndFileIdIn(Long questionId, List<Long> fileIds);

    int countByCommunityQuestionId(Long questionId);
}
