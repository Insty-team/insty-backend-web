package insty.model.course;

import insty.model.BaseEntity;
import insty.model.course.id.CourseTagId;
import insty.model.tag.Tags;
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
    @MapsId("tagId")
    @JoinColumn(name = "tag_id", nullable = false)
    private Tags tags;


    public static CourseTag create(Course course, Tags tags) {
        return CourseTag.builder()
                .courseTagId(CourseTagId.create(course.getId(), tags.getId()))
                .course(course)
                .tags(tags)
                .build();
    }
}
