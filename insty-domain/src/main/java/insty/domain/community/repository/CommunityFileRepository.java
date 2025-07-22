package insty.domain.community.repository;

import insty.model.community.CommunityFile;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface CommunityFileRepository extends JpaRepository<CommunityFile, Long> {

    List<CommunityFile> findAllByCommunityQuestionId(Long questionId);

    @Modifying
    @Transactional
    @Query("delete from CommunityFile cf where cf.communityQuestion.id = :questionId and cf.file.id in :fileIds")
    void deleteByQuestionIdAndFileIdIn(Long questionId, List<Long> fileIds);
}
