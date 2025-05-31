package insty.domain.course.repository;

import insty.model.course.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseRepository extends JpaRepository<Course, Long> {

    @Modifying
    @Query("UPDATE Course c SET c.viewCount = c.viewCount + 1 WHERE c.id = :courseId")
    void incrementViewCount(@Param("courseId") Long courseId);
}
