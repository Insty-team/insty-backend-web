package insty.domain.course.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import insty.domain.common.dto.PaginationReq;
import insty.domain.common.dto.PaginationRes;
import insty.domain.course.dto.CourseInstallEnvChecklistInfo;
import insty.domain.course.dto.CourseMySearchInfo;
import insty.domain.course.dto.CourseMySearchReq;
import insty.domain.course.dto.CourseSearchFilter;
import insty.domain.course.dto.CourseSearchInfo;
import insty.domain.course.dto.CourseSearchReq;
import insty.domain.course.repository.CourseInstallEnvChecklistRepository;
import insty.domain.course.repository.CourseKeypointRepository;
import insty.domain.course.repository.CourseQueryRepository;
import insty.domain.course.repository.CourseRepository;
import insty.domain.course.repository.CourseTagRepository;
import insty.error.CourseErrorCode;
import insty.exception.CustomException;
import insty.model.course.Course;
import insty.model.course.CourseInstallEnvChecklist;
import insty.model.course.CourseKeypoint;
import insty.model.tag.Tags;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    @Mock
    private CourseQueryRepository courseQueryRepository;

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
        Tags tag2 = Tags.create("태그2");
        when(courseTagRepository.findAllTagsByCourseId(courseId))
                .thenReturn(List.of(tag1, tag2));

        // when
        List<String> tagNames = courseReader.getTagNamesByCourseId(courseId);

        // then
        assertThat(tagNames.size()).isEqualTo(2);
        assertThat(tagNames.get(0)).isEqualTo(tag1.getTagName());
        assertThat(tagNames.get(1)).isEqualTo(tag2.getTagName());
    }

    @Test
    void getCourseById_정상() {
        // given
        Long courseId = 1L;

        // mock
        when(courseRepository.findById(courseId))
                .thenReturn(Optional.of(mock(Course.class)));

        // when
        Course course = courseReader.getCourseById(courseId);

        // then
        assertThat(course).isNotNull();
    }

    @Test
    void getCourseById_에러_강의가_존재하지_않다() {
        // given
        Long courseId = 1L;

        // mock
        when(courseRepository.findById(courseId))
                .thenReturn(Optional.empty());

        // when

        // then
        assertThatThrownBy(() -> courseReader.getCourseById(courseId))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CourseErrorCode.COURSE_NOT_FOUND);
    }

    @Test
    void searchCourse_정상() {
        // given
        int page = 1;
        int pageSize = 10;
        String search = "파이썬";
        CourseSearchReq req = new CourseSearchReq(page, pageSize, search);
        PaginationReq paginationReq = req.toPaginationReq();
        CourseSearchFilter filter = req.toSearchFilter();

        // mock
        CourseSearchInfo searchInfo = new CourseSearchInfo(1L, "파이썬 설치 강의", "설명", null, null, null);
        when(courseQueryRepository.searchCourses(paginationReq, filter))
                .thenReturn(List.of(searchInfo));
        Map<Long, List<String>> map = new HashMap<>();
        map.put(1L, List.of("태그1", "태그2"));
        when(courseQueryRepository.getCourseTags(any()))
                .thenReturn(map);

        // when
        List<CourseSearchInfo> courseSearchInfos = courseReader.searchCourse(paginationReq, filter);

        // then
        assertThat(courseSearchInfos.size()).isEqualTo(1);
        assertThat(courseSearchInfos.get(0).courseId()).isEqualTo(1L);
        assertThat(courseSearchInfos.get(0).title()).contains(search);
        assertThat(courseSearchInfos.get(0).tags()).containsExactlyInAnyOrder("태그1", "태그2");
    }

    @Test
    void countSearchCourse_정상() {
        // given
        int page = 1;
        int pageSize = 10;
        String search = "파이썬";
        CourseSearchReq req = new CourseSearchReq(page, pageSize, search);
        PaginationReq paginationReq = req.toPaginationReq();
        CourseSearchFilter filter = req.toSearchFilter();

        // mock
        when(courseQueryRepository.countSearchCourses(paginationReq, filter))
                .thenReturn(new PaginationRes(1, 1, 1, 10));

        // when
        PaginationRes paginationRes = courseReader.countSearchCourse(paginationReq, filter);

        // then
        assertThat(paginationRes).isNotNull();
        assertThat(paginationRes.totalItems()).isEqualTo(1);
        assertThat(paginationRes.totalPages()).isEqualTo(1);
        assertThat(paginationRes.currentPage()).isEqualTo(1);
        assertThat(paginationRes.perPage()).isEqualTo(10);
    }

    @Test
    void searchMyCourse_정상() {
        // given
        Long userId = 1L;
        int page = 1;
        int pageSize = 10;
        CourseMySearchReq req = new CourseMySearchReq(page, pageSize);
        PaginationReq paginationReq = req.toPaginationReq();

        // mock
        CourseMySearchInfo searchInfo = new CourseMySearchInfo(1L, "파이썬 설치 강의", 0, 100, 10L, null, null, true,
                Instant.now());
        when(courseQueryRepository.searchMyCourses(paginationReq, userId))
                .thenReturn(List.of(searchInfo));
        Map<Long, List<String>> map = new HashMap<>();
        map.put(1L, List.of("태그1", "태그2"));
        when(courseQueryRepository.getCourseTags(any()))
                .thenReturn(map);

        // when
        List<CourseMySearchInfo> courseMySearchInfos = courseReader.searchMyCourse(paginationReq, userId);

        // then
        assertThat(courseMySearchInfos.size()).isEqualTo(1);
        assertThat(courseMySearchInfos.get(0).courseId()).isEqualTo(1L);
        assertThat(courseMySearchInfos.get(0).tags()).containsExactlyInAnyOrder("태그1", "태그2");
    }

    @Test
    void countSearchMyCourse_정상() {
        // given
        Long userId = 1L;
        int page = 1;
        int pageSize = 10;
        CourseMySearchReq req = new CourseMySearchReq(page, pageSize);
        PaginationReq paginationReq = req.toPaginationReq();

        // mock
        when(courseQueryRepository.countSearchMyCourses(paginationReq, userId))
                .thenReturn(new PaginationRes(1, 1, 1, 10));

        // when
        PaginationRes paginationRes = courseReader.countSearchMyCourse(paginationReq, userId);

        // then
        assertThat(paginationRes).isNotNull();
        assertThat(paginationRes.totalItems()).isEqualTo(1);
        assertThat(paginationRes.totalPages()).isEqualTo(1);
        assertThat(paginationRes.currentPage()).isEqualTo(1);
        assertThat(paginationRes.perPage()).isEqualTo(10);
    }
}