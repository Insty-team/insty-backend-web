package insty.domain.course.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import insty.domain.common.dto.PaginationReq;
import insty.domain.common.dto.PaginationRes;
import insty.domain.course.dto.CourseMySearchInfo;
import insty.domain.course.dto.CourseSearchFilter;
import insty.domain.course.dto.CourseSearchInfo;
import insty.domain.course.repository.CourseQueryRepository;
import insty.domain.course.repository.CourseRepository;
import insty.global.property.AppProperties;
import insty.model.course.Course;
import insty.model.file.File;
import insty.model.file.FileContainerType;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class CourseComplexReaderTest {

    @InjectMocks
    private CourseComplexReader courseComplexReader;

    @Mock
    private CourseRepository courseRepository;
    @Mock
    private CourseQueryRepository courseQueryRepository;
    @Mock
    private AppProperties appProperties;

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
        // getCourseThumbnailUrlMap 테스트 세팅
        Course course = Course.create("제목", "설명", 10000, "강의 추천 대상자", true);
        ReflectionTestUtils.setField(course, "id", 1L);
        File file = File.create(FileContainerType.COURSE_THUMBNAIL, 1L, "00000000-0000-0000-0000-000000000001.jpg",
                "thumb.jpg", "image/jpeg", 10);
        ReflectionTestUtils.setField(course, "thumbnail", file);
        when(courseRepository.findWithThumbnailByCourseIdIn(any()))
                .thenReturn(List.of(course));
        when(appProperties.getDomain())
                .thenReturn("insty.test.com");

        // when
        List<CourseSearchInfo> res = courseComplexReader.searchCourse(paginationReq, req);

        // then
        assertThat(res).hasSize(1);
        assertThat(res.get(0).title()).isEqualTo("파이썬 강의");
        assertThat(res.get(0).tags()).containsExactlyInAnyOrder("태그1", "태그2");
        assertThat(res.get(0).thumbnailUrl()).isEqualTo(
                "https://insty.test.com/file/COURSE_THUMBNAIL/1/00000000-0000-0000-0000-000000000001.jpg");
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
        PaginationRes res = courseComplexReader.countSearchCourse(paginationReq, req);

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
        // getCourseThumbnailUrlMap 테스트 세팅
        Course course = Course.create("제목", "설명", 10000, "강의 추천 대상자", true);
        ReflectionTestUtils.setField(course, "id", 1L);
        File file = File.create(FileContainerType.COURSE_THUMBNAIL, 1L, "00000000-0000-0000-0000-000000000001.jpg",
                "thumb.jpg", "image/jpeg", 10);
        ReflectionTestUtils.setField(course, "thumbnail", file);
        when(courseRepository.findWithThumbnailByCourseIdIn(any()))
                .thenReturn(List.of(course));
        when(appProperties.getDomain())
                .thenReturn("insty.test.com");

        // when
        List<CourseMySearchInfo> res = courseComplexReader.searchMyCourse(paginationReq, userId);

        // then
        assertThat(res).hasSize(1);
        assertThat(res).hasSize(1);
        assertThat(res.get(0).title()).isEqualTo("파이썬 강의");
        assertThat(res.get(0).tags()).containsExactlyInAnyOrder("태그1", "태그2");
        assertThat(res.get(0).thumbnailUrl()).isEqualTo(
                "https://insty.test.com/file/COURSE_THUMBNAIL/1/00000000-0000-0000-0000-000000000001.jpg");
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
        PaginationRes res = courseComplexReader.countSearchMyCourse(paginationReq, userId);

        // then
        assertThat(res).isNotNull();
        assertThat(res).isEqualTo(paginationRes);
    }
}