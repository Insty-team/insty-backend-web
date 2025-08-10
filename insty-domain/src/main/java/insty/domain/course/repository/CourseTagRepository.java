package insty.domain.course.repository;

import insty.model.course.CourseTag;
import insty.model.tag.Tags;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseTagRepository extends JpaRepository<CourseTag, Long> {

    @Query("""
            SELECT t FROM CourseTag ct
                JOIN ct.tags t
                JOIN Course c ON c.id = ct.course.id AND c.id = :courseId
            """)
    List<Tags> findAllTagsByCourseId(@Param("courseId") Long courseId);

    @Query("""
            SELECT t.id FROM CourseTag ct
                JOIN Course c ON c.id = ct.course.id AND c.id = :courseId
                JOIN Tags t ON t.id = ct.tags.id AND t.id IN :tagIds
            """)
    Set<Long> findAllExistsTagIdsByCourseIdAndTagIdIn(@Param("courseId") Long courseId,
                                                      @Param("tagIds") List<Long> tagIds);

    void deleteAllByCourseId(Long courseId);

    void deleteAllByCourseIdIn(List<Long> courseIds);
}
