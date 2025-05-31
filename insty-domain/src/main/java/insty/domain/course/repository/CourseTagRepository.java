package insty.domain.course.repository;

import insty.model.course.CourseTag;
import insty.model.tag.Tags;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseTagRepository extends JpaRepository<CourseTag, Long> {

    @Query("""
            SELECT t FROM CourseTag ct
                JOIN ct.tags t
                JOIN ct.course c
                WHERE c.id = :courseId
            """)
    List<Tags> findAllTagsByCourseId(@Param("courseId") Long courseId);
}
