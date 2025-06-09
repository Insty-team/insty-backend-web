package insty.model.course.id;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class CoursePracticeFileIdTest {

    @Test
    void create_정상() {
        // given
        Long courseId = 1L;
        Long fileId = 1L;

        // when
        CoursePracticeFileId coursePracticeFileId = CoursePracticeFileId.create(courseId, fileId);

        // then
        assertThat(coursePracticeFileId).isNotNull();
        assertThat(coursePracticeFileId.getCourseId()).isEqualTo(courseId);
        assertThat(coursePracticeFileId.getFileId()).isEqualTo(fileId);
    }

    @Test
    void equals_hashCode_정상() {
        CoursePracticeFileId id1 = CoursePracticeFileId.create(1L, 1L);
        CoursePracticeFileId id2 = CoursePracticeFileId.create(1L, 1L);

        assertThat(id1).isEqualTo(id2);
        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
    }
}