package insty.domain.community.repository;

import insty.model.community.CommunityCommentFile;
import insty.model.community.id.CommunityCommentFileId;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommunityCommentFileRepository extends JpaRepository<CommunityCommentFile, CommunityCommentFileId> {

    List<CommunityCommentFile> findAllByCommunityComment_Id(Long commentId);

    int countByCommunityComment_Id(Long commentId);

    @Modifying
    @Query("DELETE FROM CommunityCommentFile ccf WHERE ccf.communityComment.id = :commentId AND ccf.file.id IN :fileIds")
    void deleteByCommentIdAndFileIds(@Param("commentId") Long commentId, @Param("fileIds") List<Long> fileIds);

    @Modifying
    @Query("DELETE FROM CommunityCommentFile ccf WHERE ccf.communityComment.id = :commentId")
    void deleteAllByCommentId(@Param("commentId") Long commentId);
}
