package insty.domain.course.service;

import static org.assertj.core.api.Assertions.assertThat;

import insty.cloudfront.adapter.CloudFrontSigner;
import insty.domain.course.dto.CourseInstallEnvChecklistInfo;
import insty.domain.course.dto.CoursePostReq;
import insty.domain.course.dto.CoursePostRes;
import insty.domain.course.dto.CourseUpdateReq;
import insty.domain.course.implement.CourseReader;
import insty.domain.course.implement.CourseWriter;
import insty.domain.course.repository.CourseInstallEnvChecklistRepository;
import insty.domain.course.repository.CourseKeypointRepository;
import insty.domain.course.repository.CourseRepository;
import insty.domain.course.repository.CourseTagRepository;
import insty.domain.tag.implement.TagWriter;
import insty.domain.tag.repository.TagsRepository;
import insty.global.property.AppProperties;
import insty.model.course.Course;
import insty.model.tag.Tags;
import insty.s3.adapter.S3UrlIssuer;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Tag("integration")
class CourseServiceTest {

    @Autowired
    private CourseService courseService;

    @Autowired
    private CourseReader courseReader;
    @Autowired
    private CourseWriter courseWriter;
    @Autowired
    private TagWriter tagWriter;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private CourseInstallEnvChecklistRepository courseInstallEnvChecklistRepository;
    @Autowired
    private CourseKeypointRepository courseKeypointRepository;
    @Autowired
    private CourseTagRepository courseTagRepository;
    @Autowired
    private TagsRepository tagsRepository;

    @MockitoBean
    private S3UrlIssuer s3UrlIssuer;
    @MockitoBean
    private CloudFrontSigner cloudFrontSigner;
    @MockitoBean
    private AppProperties appProperties;

    @Test
    void createCourse_정상() {
        // given
        String title = "강의 제목";
        String description = "내용 설명";
        String targetAudience = "대상자";
        int price = 10000;
        boolean isShow = true;
        CourseInstallEnvChecklistInfo checklist1 = new CourseInstallEnvChecklistInfo("설치 환경1", true);
        CourseInstallEnvChecklistInfo checklist2 = new CourseInstallEnvChecklistInfo("설치 환경2", false);
        List<CourseInstallEnvChecklistInfo> checklists = List.of(checklist1, checklist2);
        List<String> keypoints = List.of("핵심 내용1", "핵심 내용2");
        Set<String> tags = Set.of("태그1", "태그2");

        CoursePostReq req = new CoursePostReq(title, description, targetAudience, price, isShow, checklists, keypoints,
                tags);
        MockMultipartFile thumbnail = new MockMultipartFile("thumbnail", "thumb.jpg", "image/jpeg", new byte[0]);
        MockMultipartFile[] practiceFiles = new MockMultipartFile[]{
                new MockMultipartFile("practiceFile", "practice1.txt", "text/plain", "내용".getBytes())
        };

        // when
        CoursePostRes res = courseService.createCourse(req, thumbnail, practiceFiles);

        // then
        assertThat(res).isNotNull();
//        assertThat(res.courseId()).isNotNull(); // 객체 캡슐화를 지키고 id 생성 테스트는 생략
        assertThat(res.title()).isEqualTo(title);
        assertThat(res.description()).isEqualTo(description);
        assertThat(res.targetAudience()).isEqualTo(targetAudience);
        assertThat(res.price()).isEqualTo(price);
        assertThat(res.installEnvChecklist())
                .containsExactlyInAnyOrderElementsOf(checklists);
        assertThat(res.keyPoints()).hasSameElementsAs(keypoints);
        assertThat(res.tags()).hasSameElementsAs(tags);
    }

    @Sql(statements = {
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, thumbnail_id, is_show, created_at, updated_at, is_deleted) "
                    + "VALUES (100L, null, '이전 강의 제목', '이전 강의 설명', 20000, 0, 0, '파이썬 개발 환경 설치가 처음인 초보자', null, true, NOW(), NOW(), false);",
            "INSERT INTO web_service.tags (id, tag_name, created_at, updated_at) " +
                    "VALUES (100L, '존재하고 강의에 연결된 태그', NOW(), NOW())",
            "INSERT INTO web_service.tags (id, tag_name, created_at, updated_at) " +
                    "VALUES (200L, '존재하지만 강의에는 연결되지 않은 태그', NOW(), NOW())",
            "INSERT INTO web_service.course_tags (tag_id, course_id, created_at, updated_at) " +
                    "VALUES (100L, 100L, NOW(), NOW())",
            "INSERT INTO web_service.course_install_env_checklists (id, course_id, content, is_supported) " +
                    "VALUES (100L, 100L, '강의에 연결된 체크리스트', true)",
            "INSERT INTO web_service.course_install_env_checklists (id, course_id, content, is_supported) " +
                    "VALUES (200L, 100L, '강의에 연결되었지만 수정 후 없어질 체크리스트', false)",
            "INSERT INTO web_service.course_keypoints (id, course_id, content) " +
                    "VALUES (100L, 100L, '강의에 연결된 핵심포인트')",
            "INSERT INTO web_service.course_keypoints (id, course_id, content) " +
                    "VALUES (200L, 100L, '강의에 연결되었지만 수정 후 없어질 핵심포인트')"
    })
    @Test
    void updateCourse_정상() {
        // given
        Long courseId = 100L;
        String title = "새로운 강의 제목";
        String description = "새로운 강의 설명";
        String targetAudience = "새로운 대상자";
        int price = 10000;
        CourseInstallEnvChecklistInfo checklist1 = new CourseInstallEnvChecklistInfo("강의에 연결된 체크리스트", true);
        CourseInstallEnvChecklistInfo checklist2 = new CourseInstallEnvChecklistInfo("새로운 체크리스트", false);
        List<CourseInstallEnvChecklistInfo> checklists = List.of(checklist1, checklist2);
        List<String> keypoints = List.of("강의에 연결된 핵심포인트", "새로운 핵심포인트");
        Set<String> tags = Set.of("존재하고 강의에 연결된 태그", "새로운 태그");

        CourseUpdateReq req = new CourseUpdateReq(title, description, targetAudience, price, checklists, keypoints,
                tags);
        MockMultipartFile thumbnail = new MockMultipartFile("thumbnail", "thumb.jpg", "image/jpeg", new byte[0]);
        MockMultipartFile[] practiceFiles = new MockMultipartFile[]{
                new MockMultipartFile("practiceFile", "practice1.txt", "text/plain", "내용".getBytes())
        };

        // when
        CoursePostRes res = courseService.updateCourse(courseId, req, thumbnail, practiceFiles);

        // then
        assertThat(res).isNotNull();
//        assertThat(res.courseId()).isNotNull(); // 객체 캡슐화를 지키고 id 생성 테스트는 생략
        assertThat(res.title()).isEqualTo(title);
        assertThat(res.description()).isEqualTo(description);
        assertThat(res.targetAudience()).isEqualTo(targetAudience);
        assertThat(res.price()).isEqualTo(price);
        assertThat(res.installEnvChecklist())
                .containsExactlyInAnyOrderElementsOf(checklists);
        assertThat(res.keyPoints()).hasSameElementsAs(keypoints);
        assertThat(res.tags()).hasSameElementsAs(tags);

        assertThat(courseInstallEnvChecklistRepository.count()).isEqualTo(2); // 하나 삭제되고 하나 생성됨
        assertThat(courseKeypointRepository.count()).isEqualTo(2); // 하나 삭제되고 하나 생성됨
        assertThat(courseTagRepository.count()).isEqualTo(2); // 하나 삭제되고 하나 생성됨
        assertThat(tagsRepository.count()).isEqualTo(3); // 새로운 태그가 생성되어 3개가 됨
    }

    @Sql(statements = {
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, thumbnail_id, is_show, created_at, updated_at, is_deleted) "
                    + "VALUES (100L, null, '이전 강의 제목', '이전 강의 설명', 20000, 0, 0, '파이썬 개발 환경 설치가 처음인 초보자', null, true, NOW(), NOW(), false);",
            "INSERT INTO web_service.tags (id, tag_name, created_at, updated_at) " +
                    "VALUES (100L, '존재하고 강의에 연결된 태그', NOW(), NOW())",
            "INSERT INTO web_service.course_tags (tag_id, course_id, created_at, updated_at) " +
                    "VALUES (100L, 100L, NOW(), NOW())"
    })
    @Test
    void deleteCourse_정상() {
        // given
        Long courseId = 100L;

        // when
        courseService.deleteCourse(courseId);

        // then
        Optional<Course> course = courseRepository.findById(courseId);
        assertThat(course.isPresent()).isTrue();
        assertThat(course.get().isDeleted()).isTrue();

        List<Tags> allTagsByCourseId = courseTagRepository.findAllTagsByCourseId(courseId);
        assertThat(allTagsByCourseId.isEmpty()).isTrue();
    }
}