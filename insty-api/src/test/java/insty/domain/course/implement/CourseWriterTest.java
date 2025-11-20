package insty.domain.course.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import insty.domain.course.dto.CourseCreateReq;
import insty.domain.course.dto.CourseInstallEnvChecklistInfo;
import insty.domain.course.dto.CourseUpdateReq;
import insty.domain.course.repository.CourseInstallEnvChecklistRepository;
import insty.domain.course.repository.CourseKeypointRepository;
import insty.domain.course.repository.CourseRepository;
import insty.domain.course.repository.CourseTagRepository;
import insty.error.CourseErrorCode;
import insty.exception.CustomException;
import insty.model.course.Course;
import insty.model.course.CourseFixtureBuilder;
import insty.model.user.User;
import insty.model.user.UserFixtureBuilder;
import java.util.List;
import java.util.Optional;
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
        User user = UserFixtureBuilder.getUserWithId();
        String title = "제목";
        String description = "설명";
        String targetAudience = "강의 대상자";
        int price = 10000;
        boolean isShow = true;
        CourseCreateReq req = new CourseCreateReq(title, description, targetAudience, price, isShow, null, null, null,
                null);

        // mock
        when(courseRepository.save(any(Course.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Course course = courseWriter.saveCourse(user, req);

        // then
        assertThat(course).isNotNull();
//        assertThat(course.getId()).isNotNull(); // 객체 캡슐화를 지키고 id 생성 테스트는 생략
        assertThat(course.getTitle()).isEqualTo(title);
        assertThat(course.getDescription()).isEqualTo(description);
        assertThat(course.getPrice()).isEqualTo(price);
        assertThat(course.getViewCount()).isEqualTo(0);
        assertThat(course.getLikeCount()).isEqualTo(0);
        assertThat(course.getTargetAudience()).isEqualTo(targetAudience);
        assertThat(course.isShow()).isEqualTo(isShow);
    }

    @Test
    void saveCourseInstallEnvChecklist_정상() {
        // given
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();
        CourseInstallEnvChecklistInfo checklist1 = new CourseInstallEnvChecklistInfo("내용1", true);
        CourseInstallEnvChecklistInfo checklist2 = new CourseInstallEnvChecklistInfo("내용2", false);
        List<CourseInstallEnvChecklistInfo> checklistInfos = List.of(checklist1, checklist2);

        // mock
        when(courseInstallEnvChecklistRepository.saveAll(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        List<CourseInstallEnvChecklistInfo> result = courseWriter.saveCourseInstallEnvChecklist(course, checklistInfos);

        // then
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.get(0).content()).isEqualTo(checklist1.content());
        assertThat(result.get(0).isSupported()).isEqualTo(checklist1.isSupported());
        assertThat(result.get(1).content()).isEqualTo(checklist2.content());
        assertThat(result.get(1).isSupported()).isEqualTo(checklist2.isSupported());
    }

    @Test
    void saveCourseKeypoints_정상() {
        // given
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();
        List<String> keypointContents = List.of("내용1", "내용2");

        // mock
        when(courseKeypointRepository.saveAll(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        List<String> result = courseWriter.saveCourseKeypoints(course, keypointContents);

        // then
        assertThat(result.size()).isEqualTo(2);
        assertThat(result).containsExactlyInAnyOrder("내용1", "내용2");
    }

    @Test
    void updateCourse_정상() {
        // given
        Long courseId = 1L;
        String title = "제목";
        String description = "설명";
        String targetAudience = "강의 대상자";
        int price = 10000;
        CourseUpdateReq req = new CourseUpdateReq(title, description, targetAudience, price, null, null, null, null,
                null);

        // mock
        Course beforeCourse = CourseFixtureBuilder.getCourseWithIdAndUser(1L, "이전 제목", "이전 설명", 0, "이전 대상자", false);
        when(courseRepository.findById(courseId))
                .thenReturn(Optional.of(beforeCourse));
        when(courseRepository.save(any(Course.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Course course = courseWriter.updateCourse(courseId, req);

        // then
        assertThat(course).isNotNull();
        assertThat(course.getTitle()).isEqualTo(title);
        assertThat(course.getDescription()).isEqualTo(description);
        assertThat(course.getTargetAudience()).isEqualTo(targetAudience);
        assertThat(course.getPrice()).isEqualTo(price);
    }

    @Test
    void updateCourse_에러_강의가_존재하지_않다() {
        // given
        Long courseId = 1L;
        String title = "제목";
        String description = "설명";
        String targetAudience = "강의 대상자";
        int price = 10000;
        CourseUpdateReq req = new CourseUpdateReq(title, description, targetAudience, price, null, null, null, null,
                null);

        // mock
        when(courseRepository.findById(courseId))
                .thenReturn(Optional.empty());

        // when

        // then
        assertThatThrownBy(() -> courseWriter.updateCourse(courseId, req))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CourseErrorCode.COURSE_NOT_FOUND);
    }

    @Test
    void updateCourseInstallEnvChecklist_정상() {
        // given
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();
        CourseInstallEnvChecklistInfo checklist1 = new CourseInstallEnvChecklistInfo("내용1", true);
        CourseInstallEnvChecklistInfo checklist2 = new CourseInstallEnvChecklistInfo("내용2", false);
        List<CourseInstallEnvChecklistInfo> checklistInfos = List.of(checklist1, checklist2);

        // mock
        // deleteAllByCourseId를 수행하여 빈 상태가 되었다고 가정
        when(courseInstallEnvChecklistRepository.saveAll(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        List<CourseInstallEnvChecklistInfo> result = courseWriter.updateCourseInstallEnvChecklist(course,
                checklistInfos);

        // then
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.get(0).content()).isEqualTo(checklist1.content());
        assertThat(result.get(0).isSupported()).isEqualTo(checklist1.isSupported());
        assertThat(result.get(1).content()).isEqualTo(checklist2.content());
        assertThat(result.get(1).isSupported()).isEqualTo(checklist2.isSupported());
    }

    @Test
    void updateCourseKeypoints_정상() {
        // given
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();
        List<String> keypointContents = List.of("내용1", "내용2");

        // mock
        // deleteAllByCourseId를 수행하여 빈 상태가 되었다고 가정
        when(courseKeypointRepository.saveAll(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        List<String> result = courseWriter.updateCourseKeypoints(course, keypointContents);

        // then
        assertThat(result.size()).isEqualTo(2);
        assertThat(result).containsExactlyInAnyOrder("내용1", "내용2");
    }

    @Test
    void deleteCourse_정상() {
        // given
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();

        // when

        // then
        assertThatCode(() -> courseWriter.deleteCourse(course))
                .doesNotThrowAnyException();
    }

    @Test
    void patchCourseIsShow_정상(){

        //given
        Long courseId = 1L;
        boolean isShow = false;
        Course courseFixtureVisibleTrue = CourseFixtureBuilder.getCourseWithIdAndUser(); // 최초 생성시 isShow Ture
        Course courseFixtureVisibleFalse = CourseFixtureBuilder.getCourseWithIdAndUser();// 최초 생성시 isShow Ture
        //mock

        when(courseRepository.findById(courseId)).thenReturn(Optional.ofNullable(courseFixtureVisibleFalse));
        //when
        Course course = courseWriter.patchCourseIsShow(courseId, isShow);
        //then
        assertThat(courseFixtureVisibleTrue.isShow()).isTrue();
        assertThat(course.isShow()).isFalse();
    }
}