package insty.domain.community.repository;

import insty.model.community.CommunityPostFile;
import insty.model.community.id.CommunityPostFileId;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommunityPostFileRepository extends JpaRepository<CommunityPostFile, CommunityPostFileId> {

    List<CommunityPostFile> findAllByCommunityPost_Id(Long postId);

    int countByCommunityPost_Id(Long postId);

    @Modifying
    @Query("DELETE FROM CommunityPostFile cpf WHERE cpf.communityPost.id = :postId AND cpf.file.id IN :fileIds")
    void deleteByPostIdAndFileIds(@Param("postId") Long postId, @Param("fileIds") List<Long> fileIds);

    @Modifying
    @Query("DELETE FROM CommunityPostFile cpf WHERE cpf.communityPost.id = :postId")
    void deleteAllByPostId(@Param("postId") Long postId);

    @Query("""
        SELECT cpf FROM CommunityPostFile cpf
        JOIN FETCH cpf.file f
        WHERE cpf.communityPost.id IN :postIds
          AND cpf.communityPost.isDeleted = false
    """)
    List<CommunityPostFile> findAllWithFileByPostIds(@Param("postIds") List<Long> postIds);
}
