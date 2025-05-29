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
public class CourseCategoryId implements Serializable {

    private Long courseId;
    private Long categoryId;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CourseCategoryId)) {
            return false;
        }
        CourseCategoryId that = (CourseCategoryId) o;
        return Objects.equals(courseId, that.courseId) &&
                Objects.equals(categoryId, that.categoryId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseId, categoryId);
    }

    public static CourseCategoryId create(Long courseId, Long categoryId) {
        return CourseCategoryId.builder()
                .courseId(courseId)
                .categoryId(categoryId)
                .build();
    }
}
