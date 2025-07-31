package insty.domain.community.repository;

import insty.model.community.CommunityQuestion;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommunityQuestionRepository extends JpaRepository<CommunityQuestion, Long> {

    /**
     * 질문 상세 조회
     */
    // todo: 비디오 추가되면 fetch join 추가
    @Query("""
        select q from CommunityQuestion q
        join fetch q.course c
        join fetch q.user u
        left join fetch q.attachments att
        where q.id = :questionId
    """)
    Optional<CommunityQuestion> findWithCourseUserAttachmentsById(@Param("questionId") Long questionId);

    @Query("SELECT cq FROM CommunityQuestion cq WHERE cq.course.id = :courseId AND cq.isDeleted = false")
    List<CommunityQuestion> findAllByCourseId(@Param("courseId") Long courseId);
}
