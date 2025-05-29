package insty.model.course;

import insty.model.BaseEntity;
import insty.model.category.Category;
import insty.model.course.id.CourseTagId;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "course_tags", schema = "web_service")
@Getter
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CourseTag extends BaseEntity {

    @EmbeddedId
    private CourseTagId courseTagId;

    @ManyToOne
    @MapsId("courseId")
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne
    @MapsId("categoryId")
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;


    public static CourseTag create(Course course, Category category) {
        return CourseTag.builder()
                .courseTagId(CourseTagId.create(course.getId(), category.getId()))
                .course(course)
                .category(category)
                .build();
    }
}
