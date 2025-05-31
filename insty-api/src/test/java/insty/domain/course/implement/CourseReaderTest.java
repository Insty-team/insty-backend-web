package insty.domain.course.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import insty.domain.course.dto.CourseInstallEnvChecklistInfo;
import insty.domain.course.repository.CourseInstallEnvChecklistRepository;
import insty.domain.course.repository.CourseKeypointRepository;
import insty.domain.course.repository.CourseRepository;
import insty.domain.course.repository.CourseTagRepository;
import insty.model.course.CourseInstallEnvChecklist;
import insty.model.course.CourseKeypoint;
import insty.model.tag.Tags;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class CourseReaderTest {

    @InjectMocks
    private CourseReader courseReader;

    @Mock
    private CourseRepository courseRepository;
    @Mock
    private CourseInstallEnvChecklistRepository courseInstallEnvChecklistRepository;
    @Mock
    private CourseKeypointRepository courseKeypointRepository;
    @Mock
    private CourseTagRepository courseTagRepository;

    @Test
    void getChecklistsByCourseId_정상() {
        // given
        Long courseId = 1L;

        // mock
        CourseInstallEnvChecklist checklist1 = CourseInstallEnvChecklist.create(null, "내용1", true);
        CourseInstallEnvChecklist checklist2 = CourseInstallEnvChecklist.create(null, "내용2", false);
        when(courseInstallEnvChecklistRepository.findAllByCourseId(courseId))
                .thenReturn(List.of(checklist1, checklist2));

        // when
        List<CourseInstallEnvChecklistInfo> checklists = courseReader.getChecklistsByCourseId(courseId);

        // then
        assertThat(checklists.size()).isEqualTo(2);
        assertThat(checklists.get(0).content()).isEqualTo(checklist1.getContent());
        assertThat(checklists.get(0).isSupported()).isEqualTo(checklist1.isSupported());
        assertThat(checklists.get(1).content()).isEqualTo(checklist2.getContent());
        assertThat(checklists.get(1).isSupported()).isEqualTo(checklist2.isSupported());
    }

    @Test
    void getKeypointContentsByCourseId_정상() {
        // given
        Long courseId = 1L;

        // mock
        CourseKeypoint keypoint1 = CourseKeypoint.create(null, "내용1");
        CourseKeypoint keypoint2 = CourseKeypoint.create(null, "내용2");
        when(courseKeypointRepository.findAllByCourseId(courseId))
                .thenReturn(List.of(keypoint1, keypoint2));

        // when
        List<String> keypointContents = courseReader.getKeypointContentsByCourseId(courseId);

        // then
        assertThat(keypointContents.size()).isEqualTo(2);
        assertThat(keypointContents.get(0)).isEqualTo(keypoint1.getContent());
        assertThat(keypointContents.get(1)).isEqualTo(keypoint2.getContent());
    }

    @Test
    void getTagNamesByCourseId_정상() {
        // given
        Long courseId = 1L;

        // mock
        Tags tag1 = Tags.create("태그1");
        Tags tag2 = Tags.create("태그1");
        when(courseTagRepository.findAllTagsByCourseId(courseId))
                .thenReturn(List.of(tag1, tag2));

        // when
        List<String> tagNames = courseReader.getTagNamesByCourseId(courseId);

        // then
        assertThat(tagNames.size()).isEqualTo(2);
        assertThat(tagNames.get(0)).isEqualTo(tag1.getTagName());
        assertThat(tagNames.get(1)).isEqualTo(tag2.getTagName());
    }
}