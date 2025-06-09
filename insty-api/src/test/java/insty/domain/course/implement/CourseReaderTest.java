package insty.domain.course.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import insty.domain.common.dto.PaginationReq;
import insty.domain.common.dto.PaginationRes;
import insty.domain.course.dto.CourseInstallEnvChecklistInfo;
import insty.domain.course.dto.CourseMySearchInfo;
import insty.domain.course.dto.CourseSearchFilter;
import insty.domain.course.dto.CourseSearchInfo;
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
        Course course = Course.create("제목", "설명", 10000, "강의 추천 대상자", true);
        List<CourseInstallEnvChecklist> checklist = List.of(
                CourseInstallEnvChecklist.create(course, "체크리스트", true),
                CourseInstallEnvChecklist.create(course, "체크리스트2", false)
        );
        when(courseInstallEnvChecklistRepository.findAllByCourseId(courseId))
                .thenReturn(checklist);

        // when
        List<CourseInstallEnvChecklistInfo> res = courseReader.getChecklistsByCourseId(courseId);

        // then
        assertThat(res).hasSize(2);
        assertThat(res.get(0).content()).isEqualTo("체크리스트");
        assertThat(res.get(0).isSupported()).isTrue();
    }

    @Test
    void getKeypointContentsByCourseId_정상() {
        // given
        Long courseId = 1L;

        // mock
        Course course = Course.create("제목", "설명", 10000, "강의 추천 대상자", true);
        CourseKeypoint courseKeypoint = CourseKeypoint.create(course, "핵심포인트");
        when(courseKeypointRepository.findAllByCourseId(courseId))
                .thenReturn(List.of(courseKeypoint));

        // when
        List<String> contents = courseReader.getKeypointContentsByCourseId(courseId);

        // then
        assertThat(contents).hasSize(1);
        assertThat(contents).containsExactly("핵심포인트");
    }

    @Test
    void getTagNamesByCourseId_정상() {
        // given
        Long courseId = 1L;

        // mock
        Tags tag = Tags.create("태그");
        when(courseTagRepository.findAllTagsByCourseId(courseId))
                .thenReturn(List.of(tag));

        // when
        List<String> tagNames = courseReader.getTagNamesByCourseId(courseId);

        // then
        assertThat(tagNames).hasSize(1);
        assertThat(tagNames).containsExactly("태그");
    }

    @Test
    void getCourseById_정상() {
        // given
        Long courseId = 1L;

        // mock
        Course course = Course.create("제목", "설명", 10000, "강의 추천 대상자", true);
        when(courseRepository.findById(courseId))
                .thenReturn(Optional.of(course));

        // when
        Course res = courseReader.getCourseById(courseId);

        // then
        assertThat(res).isNotNull();
    }

    @Test
    void getCourseById_에러_존재하지_않는_강의() {
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
        PaginationReq paginationReq = new PaginationReq(1, 10);
        CourseSearchFilter req = new CourseSearchFilter("파이썬");

        // mock
        CourseSearchInfo searchInfo = new CourseSearchInfo(1L, "파이썬 강의", "설명", null, null, null);
        when(courseQueryRepository.searchCourses(paginationReq, req))
                .thenReturn(List.of(searchInfo));
        Map<Long, List<String>> courseTag = Map.of(1L, List.of("태그1", "태그2"));
        when(courseQueryRepository.getCourseTags(any()))
                .thenReturn(courseTag);

        // when
        List<CourseSearchInfo> res = courseReader.searchCourse(paginationReq, req);

        // then
        assertThat(res).hasSize(1);
        assertThat(res.get(0).title()).isEqualTo("파이썬 강의");
        assertThat(res.get(0).tags()).containsExactlyInAnyOrder("태그1", "태그2");
    }

    @Test
    void countSearchCourse_정상() {
        // given
        PaginationReq paginationReq = new PaginationReq(1, 10);
        CourseSearchFilter req = new CourseSearchFilter("파이썬");

        // mock
        PaginationRes paginationRes = new PaginationRes(1, 1, 1, 10);
        when(courseQueryRepository.countSearchCourses(paginationReq, req))
                .thenReturn(paginationRes);

        // when
        PaginationRes res = courseReader.countSearchCourse(paginationReq, req);

        // then
        assertThat(res).isNotNull();
        assertThat(res).isEqualTo(paginationRes);
    }

    @Test
    void searchMyCourse_정상() {
        // given
        PaginationReq paginationReq = new PaginationReq(1, 10);
        Long userId = 1L;

        // mock
        CourseMySearchInfo searchInfo = new CourseMySearchInfo(1L, "파이썬 강의", 1000, 1, null, null, null, true,
                Instant.now());
        when(courseQueryRepository.searchMyCourses(paginationReq, userId))
                .thenReturn(List.of(searchInfo));
        Map<Long, List<String>> courseTag = Map.of(1L, List.of("태그1", "태그2"));
        when(courseQueryRepository.getCourseTags(any()))
                .thenReturn(courseTag);

        // when
        List<CourseMySearchInfo> res = courseReader.searchMyCourse(paginationReq, userId);

        // then
        assertThat(res).hasSize(1);
        assertThat(res).hasSize(1);
        assertThat(res.get(0).title()).isEqualTo("파이썬 강의");
        assertThat(res.get(0).tags()).containsExactlyInAnyOrder("태그1", "태그2");
    }

    @Test
    void countSearchMyCourse_정상() {
        // given
        PaginationReq paginationReq = new PaginationReq(1, 10);
        Long userId = 1L;

        // mock
        PaginationRes paginationRes = new PaginationRes(1, 1, 1, 10);
        when(courseQueryRepository.countSearchMyCourses(paginationReq, userId))
                .thenReturn(paginationRes);

        // when
        PaginationRes res = courseReader.countSearchMyCourse(paginationReq, userId);

        // then
        assertThat(res).isNotNull();
        assertThat(res).isEqualTo(paginationRes);
    }
}
