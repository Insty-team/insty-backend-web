package insty.domain.course.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.when;

import insty.domain.course.repository.CourseTagRepository;
import insty.domain.tag.implement.TagWriter;
import insty.model.course.Course;
import insty.model.course.CourseFixtureBuilder;
import insty.model.tag.Tags;
import insty.model.tag.TagsFixtureBuilder;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class CourseTagWriterTest {

    @InjectMocks
    private CourseTagWriter courseTagWriter;

    @Mock
    private TagWriter tagWriter;
    @Mock
    private CourseTagRepository courseTagRepository;

    @Test
    void saveCourseTagsAndGetTagNames_정상() {
        // given
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();
        Set<String> tagNames = Set.of("태그1", "태그2");

        // mock
        Tags tag1 = TagsFixtureBuilder.getTagsWithId(1L, "태그1");
        Tags tag2 = TagsFixtureBuilder.getTagsWithId(2L, "태그2");
        when(tagWriter.saveTags(tagNames))
                .thenReturn(Set.of(tag1, tag2));

        // when
        List<String> tags = courseTagWriter.saveCourseTagsAndGetTagNames(course, tagNames);

        // then
        assertThat(tags).hasSize(2);
        assertThat(tags).containsExactlyInAnyOrder("태그1", "태그2");
    }

    @Test
    void updateCourseTags_정상() {
        // given
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();
        Set<String> tagNames = Set.of("태그1", "태그2");

        // mock
        Tags tag1 = TagsFixtureBuilder.getTagsWithId(1L, "태그1");
        Tags tag2 = TagsFixtureBuilder.getTagsWithId(2L, "태그2");
        when(tagWriter.saveTags(tagNames))
                .thenReturn(Set.of(tag1, tag2));

        // when
        List<String> tags = courseTagWriter.updateCourseTags(course, tagNames);

        // then
        assertThat(tags).hasSize(2);
        assertThat(tags).containsExactlyInAnyOrder("태그1", "태그2");
    }

    @Test
    void deleteAllCourseTags_정상() {
        // given
        Long courseId = 1L;

        // when

        // then
        assertThatCode(() -> courseTagWriter.deleteAllCourseTags(courseId))
                .doesNotThrowAnyException();
    }
}