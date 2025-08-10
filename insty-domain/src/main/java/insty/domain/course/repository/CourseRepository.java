package insty.domain.course.repository;

import insty.model.course.Course;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseRepository extends JpaRepository<Course, Long> {

    @Modifying
    @Query("UPDATE Course c SET c.viewCount = c.viewCount + 1 WHERE c.id = :courseId")
    void incrementViewCount(@Param("courseId") Long courseId);

    boolean existsByIdAndUserId(Long courseId, Long userId);

    @Query("SELECT c FROM Course c JOIN FETCH c.thumbnail WHERE c.id IN :courseIds")
    List<Course> findWithThumbnailByCourseIdIn(@Param("courseIds") List<Long> courseIds);

    List<Long> findAllIdByUserId(Long userId);
}
