package insty.domain.course.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import insty.domain.course.repository.CourseTagRepository;
import insty.domain.tag.implement.TagWriter;
import insty.model.course.Course;
import insty.model.tag.Tags;
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
    void saveCourseTagsAndGetTags_정상() {
        // given
        Course course = mock(Course.class);
        Set<String> tagNames = Set.of("태그1", "태그2");

        // mock
        when(tagWriter.saveTags(tagNames))
                .thenReturn(Set.of(Tags.create("태그1"), Tags.create("태그2")));

        // when
        Set<Tags> tags = courseTagWriter.saveCourseTagsAndGetTags(course, tagNames);

        // then
        assertThat(tags).hasSize(2);
        List<String> savedTagNames = tags.stream()
                .map(Tags::getTagName)
                .toList();
        assertThat(savedTagNames).containsExactlyInAnyOrder("태그1", "태그2");
    }
}