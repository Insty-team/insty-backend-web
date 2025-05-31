package insty.domain.course.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import insty.domain.course.dto.CourseInstallEnvChecklistInfo;
import insty.domain.course.dto.CoursePostReq;
import insty.domain.course.repository.CourseInstallEnvChecklistRepository;
import insty.domain.course.repository.CourseKeypointRepository;
import insty.domain.course.repository.CourseRepository;
import insty.domain.course.repository.CourseTagRepository;
import insty.model.course.Course;
import insty.model.course.CourseInstallEnvChecklist;
import insty.model.course.CourseKeypoint;
import insty.model.course.CourseTag;
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
class CourseWriterTest {

    @InjectMocks
    private CourseWriter courseWriter;

    @Mock
    private CourseRepository courseRepository;
    @Mock
    private CourseInstallEnvChecklistRepository courseInstallEnvChecklistRepository;
    @Mock
    private CourseKeypointRepository courseKeypointRepository;
    @Mock
    private CourseTagRepository courseTagRepository;

    @Test
    void saveCourse_정상() {
        // given
        String title = "제목";
        String description = "설명";
        String targetAudience = "강의 대상자";
        int price = 10000;
        boolean isShow = true;
        CoursePostReq req = new CoursePostReq(title, description, targetAudience, price, isShow, null, null, null);
        Long thumbnailId = null;

        // mock
        when(courseRepository.save(any(Course.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Course course = courseWriter.saveCourse(req, thumbnailId);

        // then
        assertThat(course).isNotNull();
//        assertThat(course.getId()).isNotNull(); // 객체 캡슐화를 지키고 id 생성 테스트는 생략
        assertThat(course.getTitle()).isEqualTo(title);
        assertThat(course.getDescription()).isEqualTo(description);
        assertThat(course.getPrice()).isEqualTo(price);
        assertThat(course.getViewCount()).isEqualTo(0);
        assertThat(course.getLikeCount()).isEqualTo(0);
        assertThat(course.getTargetAudience()).isEqualTo(targetAudience);
        assertThat(course.getThumbnailId()).isEqualTo(thumbnailId);
        assertThat(course.isShow()).isEqualTo(isShow);
    }

    @Test
    void saveCourseInstallEnvChecklist_정상() {
        // given
        Course course = mock(Course.class);
        CourseInstallEnvChecklistInfo checklist1 = new CourseInstallEnvChecklistInfo("내용1", true);
        CourseInstallEnvChecklistInfo checklist2 = new CourseInstallEnvChecklistInfo("내용2", false);
        List<CourseInstallEnvChecklistInfo> checklistInfos = List.of(checklist1, checklist2);

        // mock
        when(courseInstallEnvChecklistRepository.save(any(CourseInstallEnvChecklist.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        List<CourseInstallEnvChecklist> result = courseWriter.saveCourseInstallEnvChecklist(course, checklistInfos);

        // then
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.get(0).getContent()).isEqualTo(checklist1.content());
        assertThat(result.get(0).isSupported()).isEqualTo(checklist1.isSupported());
        assertThat(result.get(1).getContent()).isEqualTo(checklist2.content());
        assertThat(result.get(1).isSupported()).isEqualTo(checklist2.isSupported());
    }

    @Test
    void saveCourseKeypoints_정상() {
        // given
        Course course = mock(Course.class);
        List<String> keypointContents = List.of("내용1", "내용2");

        // mock
        when(courseKeypointRepository.save(any(CourseKeypoint.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        List<CourseKeypoint> result = courseWriter.saveCourseKeypoints(course, keypointContents);

        // then
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.get(0).getContent()).isEqualTo(keypointContents.get(0));
        assertThat(result.get(1).getContent()).isEqualTo(keypointContents.get(1));
    }

    @Test
    void saveCourseTags_정상() {
        // given
        Course course = mock(Course.class);
        Tags tags1 = Tags.create("태그1");
        Tags tags2 = Tags.create("태그2");
        List<Tags> tags = List.of(tags1, tags2);

        // mock
        when(courseTagRepository.save(any(CourseTag.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when

        // then
        assertThatCode(() -> courseWriter.saveCourseTags(course, tags))
                .doesNotThrowAnyException();
    }
}