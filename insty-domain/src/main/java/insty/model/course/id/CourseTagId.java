package insty.model.course.id;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CourseTagId implements Serializable {

    private Long courseId;
    private Long tagId;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CourseTagId)) {
            return false;
        }
        CourseTagId that = (CourseTagId) o;
        return Objects.equals(courseId, that.courseId) &&
                Objects.equals(tagId, that.tagId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseId, tagId);
    }

    public static CourseTagId create(Long courseId, Long tagId) {
        return CourseTagId.builder()
                .courseId(courseId)
                .tagId(tagId)
                .build();
    }
}
