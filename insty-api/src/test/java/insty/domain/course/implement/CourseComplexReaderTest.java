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
import insty.model.course.CourseFixtureBuilder;
import insty.model.file.File;
import insty.model.file.FileFixtureBuilder;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();
        File thumbnail = FileFixtureBuilder.getCourseThumbnailWithId();
        ReflectionTestUtils.setField(course, "thumbnail", thumbnail);
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
        CourseMySearchInfo searchInfo = new CourseMySearchInfo(1L, "파이썬 강의", 1000, 1, 5L, null, null, true,
                Instant.now());
        when(courseQueryRepository.searchMyCourses(paginationReq, userId))
                .thenReturn(List.of(searchInfo));
        Map<Long, List<String>> courseTag = Map.of(1L, List.of("태그1", "태그2"));
        when(courseQueryRepository.getCourseTags(any()))
                .thenReturn(courseTag);
        // getCourseThumbnailUrlMap 테스트 세팅
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();
        File thumbnail = FileFixtureBuilder.getCourseThumbnailWithId();
        ReflectionTestUtils.setField(course, "thumbnail", thumbnail);
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

    @Test
    void setBasicThumbnailUrlForSearch_정상() {
        // given
        CourseSearchInfo searchInfo1 = new CourseSearchInfo(1L, "사용자가 썸네일을 업로드한 강의", "설명", null, "업로드된 썸네일 url", null);
        CourseSearchInfo searchInfo2 = new CourseSearchInfo(2L, "사용자가 썸네일을 업로드하지 않아 기본썸네일이 제공되는 강의", "설명", null, null,
                null);
        CourseSearchInfo searchInfo3 = new CourseSearchInfo(3L, "연결된 영상이 없는 강의", "설명", null, null, null);
        List<CourseSearchInfo> searchInfo = List.of(searchInfo1, searchInfo2, searchInfo3);

        // mock
        when(appProperties.getDomain())
                .thenReturn("insty.test.com");
        when(courseQueryRepository.getCourseVideoUuids(any()))
                .thenReturn(Map.of(2L, UUID.fromString("00000000-0000-0000-0000-000000000001")));

        // when
        List<CourseSearchInfo> result = courseComplexReader.setBasicThumbnailUrlForSearch(searchInfo);

        // then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).thumbnailUrl()).isEqualTo("업로드된 썸네일 url");
        assertThat(result.get(1).thumbnailUrl()).contains("00000000-0000-0000-0000-000000000001");
        assertThat(result.get(2).thumbnailUrl()).isNull();
    }

    @Test
    void setBasicThumbnailUrlForMy_정상() {
        // given
        CourseMySearchInfo searchInfo1 = new CourseMySearchInfo(1L, "사용자가 썸네일을 업로드한 강의", 1000, 1, 5L, null,
                "업로드된 썸네일 url", true, Instant.now());
        CourseMySearchInfo searchInfo2 = new CourseMySearchInfo(2L, "사용자가 썸네일을 업로드하지 않아 기본썸네일이 제공되는 강의", 1000, 1, 5L,
                null, null, true, Instant.now());
        CourseMySearchInfo searchInfo3 = new CourseMySearchInfo(3L, "연결된 영상이 없는 강의", 1000, 1, 5L, null, null, true,
                Instant.now());
        List<CourseMySearchInfo> searchInfo = List.of(searchInfo1, searchInfo2, searchInfo3);

        // mock
        when(appProperties.getDomain())
                .thenReturn("insty.test.com");
        when(courseQueryRepository.getCourseVideoUuids(any()))
                .thenReturn(Map.of(2L, UUID.fromString("00000000-0000-0000-0000-000000000001")));

        // when
        List<CourseMySearchInfo> result = courseComplexReader.setBasicThumbnailUrlForMy(searchInfo);

        // then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).thumbnailUrl()).isEqualTo("업로드된 썸네일 url");
        assertThat(result.get(1).thumbnailUrl()).contains("00000000-0000-0000-0000-000000000001");
        assertThat(result.get(2).thumbnailUrl()).isNull();
    }
}