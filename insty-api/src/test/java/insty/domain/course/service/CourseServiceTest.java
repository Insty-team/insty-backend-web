package insty.domain.course.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import insty.cloudfront.adapter.CloudFrontSigner;
import insty.domain.common.SearchRes;
import insty.domain.common.dto.PaginationRes;
import insty.domain.course.dto.CourseCreateReq;
import insty.domain.course.dto.CourseDetailRes;
import insty.domain.course.dto.CourseInstallEnvChecklistInfo;
import insty.domain.course.dto.CourseMySearchInfo;
import insty.domain.course.dto.CourseMySearchReq;
import insty.domain.course.dto.CourseSearchInfo;
import insty.domain.course.dto.CourseSearchReq;
import insty.domain.course.dto.CourseUpdateReq;
import insty.domain.course.implement.CourseCounter;
import insty.domain.course.implement.CourseFileReader;
import insty.domain.course.implement.CourseFileWriter;
import insty.domain.course.implement.CourseReader;
import insty.domain.course.implement.CourseTagWriter;
import insty.domain.course.implement.CourseWriter;
import insty.domain.course.repository.CourseInstallEnvChecklistRepository;
import insty.domain.course.repository.CourseKeypointRepository;
import insty.domain.course.repository.CoursePracticeFileRepository;
import insty.domain.course.repository.CourseRepository;
import insty.domain.course.repository.CourseTagRepository;
import insty.domain.file.implement.FileWriter;
import insty.domain.file.repository.FileRepository;
import insty.domain.tag.implement.TagWriter;
import insty.domain.tag.repository.TagsRepository;
import insty.domain.video.repository.VideoCourseRepository;
import insty.global.property.AppProperties;
import insty.model.course.Course;
import insty.model.course.CourseInstallEnvChecklist;
import insty.model.course.CourseKeypoint;
import insty.model.course.CoursePracticeFile;
import insty.model.file.File;
import insty.model.tag.Tags;
import insty.model.video.VideoCourse;
import insty.model.video.VideoType;
import insty.s3.adapter.S3FileManager;
import insty.s3.adapter.S3UrlIssuer;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    private CourseCounter courseCounter;
    @Autowired
    private CourseFileWriter courseFileWriter;
    @Autowired
    private FileWriter fileWriter;
    @Autowired
    private CourseFileReader courseFileReader;
    @Autowired
    private CourseTagWriter courseTagWriter;
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
    @Autowired
    private CoursePracticeFileRepository coursePracticeFileRepository;
    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private VideoCourseRepository videoCourseRepository;

    @MockitoBean
    private S3UrlIssuer s3UrlIssuer;
    @MockitoBean
    private S3FileManager s3FileManager;
    @MockitoBean
    private CloudFrontSigner cloudFrontSigner;
    @MockitoBean
    private AppProperties appProperties;

    @Sql(statements = {
            "INSERT INTO shared.video_courses (id, video_uuid, course_id, s3key, extension, original_file_name, duration, encoding_status, encoding_at, analysis_status, analysis_at, created_at, updated_at, is_deleted) "
                    + "VALUES (1L, '00000000-0000-0000-0000-000000000001', null, 'vod/COURSE/mp4/00000000-0000-0000-0000-000000000001/course_video.mp4', 'mp4', 'course_video.mp4', 10, 'PROCESSING', NOW(), 'WAITING', NOW(), NOW(), NOW(), false)"
    })
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
        UUID videoUuid = UUID.fromString("00000000-0000-0000-0000-000000000001");

        CourseCreateReq req = new CourseCreateReq(title, description, targetAudience, price, isShow, checklists,
                keypoints, tags, videoUuid);
        MockMultipartFile thumbnail = new MockMultipartFile("thumbnail", "thumb.jpg", "image/jpeg",
                "content".getBytes());
        List<MultipartFile> practiceFiles = List.of(
                new MockMultipartFile("practiceFile", "practice1.jpg", "image/jpeg", "내용".getBytes()));

        // mock
        when(appProperties.getDomain())
                .thenReturn("insty.test.com");
        when(s3FileManager.upload(any(), anyString(), anyString()))
                .thenReturn("00000000-0000-0000-0000-000000000001.jpg");

        // when
        CourseDetailRes res = courseService.createCourse(req, thumbnail, practiceFiles);

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
        assertThat(res.videoType()).isEqualTo(VideoType.COURSE);
        assertThat(res.thumbnailUrl()).isEqualTo(
                "https://insty.test.com/file/COURSE_THUMBNAIL/1/00000000-0000-0000-0000-000000000001.jpg");
        assertThat(res.practiceFile().size()).isEqualTo(practiceFiles.size());
        assertThat(res.practiceFile().get(0).name()).isEqualTo(practiceFiles.get(0).getOriginalFilename());
        assertThat(res.practiceFile().get(0).contentType()).isEqualTo(practiceFiles.get(0).getContentType());
        assertThat(res.practiceFile().get(0).size()).isGreaterThan(0);
        assertThat(res.practiceFile().get(0).url()).isEqualTo(
                "https://insty.test.com/file/COURSE_PRACTICE_FILE/1/00000000-0000-0000-0000-000000000001.jpg");

        Optional<VideoCourse> videoCourse = videoCourseRepository.findById(1L);
        assertThat(videoCourse.isPresent()).isTrue();
        assertThat(videoCourse.get().getCourse().getId()).isNotNull();
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
                    "VALUES (200L, 100L, '강의에 연결되었지만 수정 후 없어질 핵심포인트')",
            "INSERT INTO shared.video_courses (id, video_uuid, course_id, s3key, extension, original_file_name, duration, encoding_status, encoding_at, analysis_status, analysis_at, created_at, updated_at, is_deleted) "
                    + "VALUES (1L, '00000000-0000-0000-0000-000000000001', 100L, 'vod/COURSE/mp4/00000000-0000-0000-0000-000000000001/course_video.mp4', 'mp4', 'course_video.mp4', 10, 'COMPLETED', NOW(), 'COMPLETED', NOW(), NOW(), NOW(), false)",
            "INSERT INTO shared.video_courses (id, video_uuid, course_id, s3key, extension, original_file_name, duration, encoding_status, encoding_at, analysis_status, analysis_at, created_at, updated_at, is_deleted) "
                    + "VALUES (2L, '00000000-0000-0000-0000-000000000002', null, 'vod/COURSE/mp4/00000000-0000-0000-0000-000000000002/new_course_video.mp4', 'mp4', 'new_course_video.mp4', 10, 'PROCESSING', NOW(), 'WAITING', NOW(), NOW(), NOW(), false)"
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
        UUID updateVideoUuid = UUID.fromString("00000000-0000-0000-0000-000000000002");

        CourseUpdateReq req = new CourseUpdateReq(title, description, targetAudience, price, checklists, keypoints,
                tags, null, updateVideoUuid);
        MockMultipartFile thumbnail = new MockMultipartFile("thumbnail", "thumb.jpg", "image/jpeg",
                "content".getBytes());
        List<MultipartFile> practiceFiles = List.of(
                new MockMultipartFile("practiceFile", "practice1.jpg", "image/jpeg", "내용".getBytes()));

        // mock
        when(appProperties.getDomain())
                .thenReturn("insty.test.com");
        when(s3FileManager.upload(any(), anyString(), anyString()))
                .thenReturn("00000000-0000-0000-0000-000000000001.jpg");

        // when
        CourseDetailRes res = courseService.updateCourse(courseId, req, thumbnail, practiceFiles);

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
        assertThat(res.videoType()).isEqualTo(VideoType.COURSE);
        assertThat(res.thumbnailUrl()).isEqualTo(
                "https://insty.test.com/file/COURSE_THUMBNAIL/100/00000000-0000-0000-0000-000000000001.jpg");
        assertThat(res.practiceFile().size()).isEqualTo(practiceFiles.size());
        assertThat(res.practiceFile().get(0).name()).isEqualTo(practiceFiles.get(0).getOriginalFilename());
        assertThat(res.practiceFile().get(0).contentType()).isEqualTo(practiceFiles.get(0).getContentType());
        assertThat(res.practiceFile().get(0).size()).isGreaterThan(0);
        assertThat(res.practiceFile().get(0).url()).isEqualTo(
                "https://insty.test.com/file/COURSE_PRACTICE_FILE/100/00000000-0000-0000-0000-000000000001.jpg");

        assertThat(courseInstallEnvChecklistRepository.count()).isEqualTo(2); // 하나 삭제되고 하나 생성됨
        assertThat(courseKeypointRepository.count()).isEqualTo(2); // 하나 삭제되고 하나 생성됨
        assertThat(courseTagRepository.count()).isEqualTo(2); // 1개 삭제되고 2개 생성됨
        assertThat(tagsRepository.count()).isEqualTo(3); // 새로운 태그가 생성되어 3개가 됨

        assertThat(videoCourseRepository.count()).isEqualTo(2); // 하나는 가상삭제, 하나는 새로 생성
        Optional<UUID> videoUuid = videoCourseRepository.findVideoUuidByCourseId(100L);
        assertThat(videoUuid.isPresent()).isTrue();
        assertThat(videoUuid.get().toString()).isEqualTo("00000000-0000-0000-0000-000000000002");
    }

    @Sql(statements = {
            "INSERT INTO web_service.files (id, container_type, container_id, name, original_name, content_type, size, created_at, updated_at) "
                    + "VALUES (1L, 'COURSE_THUMBNAIL', 1L, '00000000-0000-0000-0000-000000000001.jpg', 'thumbnail.jpg', 'image/jpeg', 20, NOW(), NOW())",
            "INSERT INTO web_service.files (id, container_type, container_id, name, original_name, content_type, size, created_at, updated_at) "
                    + "VALUES (2L, 'COURSE_PRACTICE_FILE', 1L, '00000000-0000-0000-0000-000000000002.jpg', 'practice.jpg', 'image/jpeg', 30, NOW(), NOW())",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, thumbnail_id, is_show, created_at, updated_at, is_deleted) "
                    + "VALUES (1L, null, '이전 강의 제목', '이전 강의 설명', 20000, 0, 0, '파이썬 개발 환경 설치가 처음인 초보자', null, true, NOW(), NOW(), false);",
            "INSERT INTO web_service.tags (id, tag_name, created_at, updated_at) " +
                    "VALUES (1L, '존재하고 강의에 연결된 태그', NOW(), NOW())",
            "INSERT INTO web_service.course_tags (tag_id, course_id, created_at, updated_at) " +
                    "VALUES (1L, 1L, NOW(), NOW())",
            "INSERT INTO web_service.course_practice_files (course_id, file_id, created_at, updated_at) " +
                    "VALUES (1L, 2L, NOW(), NOW())"
    })
    @Test
    void deleteCourse_정상() {
        // given
        Long courseId = 1L;

        // when
        courseService.deleteCourse(courseId);

        // then
        Optional<Course> course = courseRepository.findById(courseId);
        assertThat(course.isPresent()).isTrue();
        assertThat(course.get().isDeleted()).isTrue();
        assertThat(course.get().getThumbnail()).isNull();

        List<Tags> allTagsByCourseId = courseTagRepository.findAllTagsByCourseId(courseId);
        assertThat(allTagsByCourseId.isEmpty()).isTrue();

        List<CoursePracticeFile> coursePracticeFile = coursePracticeFileRepository.findAll();
        assertThat(coursePracticeFile.isEmpty()).isTrue();

        List<File> files = fileRepository.findAll();
        assertThat(files.isEmpty()).isTrue();
    }

    @Sql(statements = {
            "INSERT INTO web_service.files (id, container_type, container_id, name, original_name, content_type, size, created_at, updated_at) "
                    + "VALUES (1L, 'COURSE_THUMBNAIL', 1L, '00000000-0000-0000-0000-000000000001.jpg', 'thumbnail.jpg', 'image/jpeg', 20, NOW(), NOW())",
            "INSERT INTO web_service.files (id, container_type, container_id, name, original_name, content_type, size, created_at, updated_at) "
                    + "VALUES (2L, 'COURSE_PRACTICE_FILE', 1L, '00000000-0000-0000-0000-000000000002.jpg', 'practice.jpg', 'image/jpeg', 30, NOW(), NOW())",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, thumbnail_id, is_show, created_at, updated_at, is_deleted) "
                    + "VALUES (1L, null, '이전 강의 제목', '이전 강의 설명', 20000, 0, 0, '파이썬 개발 환경 설치가 처음인 초보자', 1L, true, NOW(), NOW(), false);",
            "INSERT INTO web_service.tags (id, tag_name, created_at, updated_at) " +
                    "VALUES (1L, '존재하고 강의에 연결된 태그', NOW(), NOW())",
            "INSERT INTO web_service.course_tags (tag_id, course_id, created_at, updated_at) " +
                    "VALUES (1L, 1L, NOW(), NOW())",
            "INSERT INTO web_service.course_install_env_checklists (id, course_id, content, is_supported) " +
                    "VALUES (1L, 1L, '강의에 연결된 체크리스트', true)",
            "INSERT INTO web_service.course_keypoints (id, course_id, content) " +
                    "VALUES (1L, 1L, '강의에 연결된 핵심포인트')",
            "INSERT INTO web_service.course_practice_files (course_id, file_id, created_at, updated_at) " +
                    "VALUES (1L, 2L, NOW(), NOW())"
    })
    @Test
    void detailCourse_정상() {
        // given
        Long courseId = 1L;

        // mock
        when(appProperties.getDomain())
                .thenReturn("insty.test.com");

        // when
        CourseDetailRes res = courseService.detailCourse(courseId);

        // then
        Optional<Course> course = courseRepository.findById(courseId);
        assertThat(course.isPresent()).isTrue();
        assertThat(course.get().getViewCount()).isEqualTo(1);

        assertThat(res).isNotNull();
        assertThat(res.title()).isEqualTo(course.get().getTitle());
        assertThat(res.description()).isEqualTo(course.get().getDescription());
        assertThat(res.targetAudience()).isEqualTo(course.get().getTargetAudience());
        assertThat(res.price()).isEqualTo(course.get().getPrice());

        List<CourseInstallEnvChecklist> checklists = courseInstallEnvChecklistRepository.findAllByCourseId(courseId);
        assertThat(res.installEnvChecklist().size()).isEqualTo(1);
        assertThat(res.installEnvChecklist().get(0).content()).isEqualTo(checklists.get(0).getContent());

        List<CourseKeypoint> keypoints = courseKeypointRepository.findAllByCourseId(courseId);
        assertThat(res.keyPoints().size()).isEqualTo(1);
        assertThat(res.keyPoints().get(0)).isEqualTo(keypoints.get(0).getContent());

        List<Tags> tags = courseTagRepository.findAllTagsByCourseId(courseId);
        assertThat(res.tags().size()).isEqualTo(1);
        assertThat(res.tags().get(0)).isEqualTo(tags.get(0).getTagName());

        assertThat(res.videoType()).isEqualTo(VideoType.COURSE);
        assertThat(res.createdAt()).isEqualTo(course.get().getCreatedAt());

        assertThat(res.thumbnailUrl()).isEqualTo(
                "https://insty.test.com/file/COURSE_THUMBNAIL/1/00000000-0000-0000-0000-000000000001.jpg");
        assertThat(res.practiceFile().size()).isEqualTo(1);
        assertThat(res.practiceFile().get(0).url()).isEqualTo(
                "https://insty.test.com/file/COURSE_PRACTICE_FILE/1/00000000-0000-0000-0000-000000000002.jpg");
        assertThat(res.practiceFile().get(0).name()).isEqualTo("practice.jpg");
    }

    @Sql(statements = {
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, thumbnail_id, is_show, created_at, updated_at, is_deleted) "
                    + "VALUES (1L, null, '파이썬 설치 강의', '설명', 20000, 0, 0, '파이썬 개발 환경 설치가 처음인 초보자', null, true, NOW(), NOW(), false);",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, thumbnail_id, is_show, created_at, updated_at, is_deleted) "
                    + "VALUES (2L, null, '자바 설치 강의', '설명', 20000, 0, 0, '자바 개발 환경 설치가 처음인 초보자', null, true, NOW(), NOW(), false);",
            "INSERT INTO web_service.tags (id, tag_name, created_at, updated_at) " +
                    "VALUES (1L, '존재하고 강의에 연결된 태그', NOW(), NOW())",
            "INSERT INTO web_service.tags (id, tag_name, created_at, updated_at) " +
                    "VALUES (2L, '존재하지만 강의에는 연결되지 않은 태그', NOW(), NOW())",
            "INSERT INTO web_service.course_tags (tag_id, course_id, created_at, updated_at) " +
                    "VALUES (1L, 1L, NOW(), NOW())"
    })
    @Test
    void searchCourse_정상() {
        // given
        int page = 1;
        int pageSize = 10;
        String search = "파이썬";
        CourseSearchReq req = new CourseSearchReq(page, pageSize, search);

        // when
        SearchRes<CourseSearchInfo> res = courseService.searchCourse(req);

        // then
        List<CourseSearchInfo> items = res.items();
        PaginationRes pagination = res.pagination();

        assertThat(pagination).isNotNull();
        assertThat(pagination.totalItems()).isEqualTo(1);
        assertThat(pagination.totalPages()).isEqualTo(1);
        assertThat(pagination.currentPage()).isEqualTo(1);
        assertThat(pagination.perPage()).isEqualTo(10);

        assertThat(items).isNotNull();
        assertThat(items.size()).isEqualTo(1);
        assertThat(items.get(0).title()).contains(search);
        assertThat(items.get(0).tags()).containsExactlyInAnyOrder("존재하고 강의에 연결된 태그");
    }

    @Sql(statements = {
            "INSERT INTO shared.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) "
                    + "VALUES (1L, 'example@example.com', 'example', 1234, null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO shared.users (id, email, nickname, password, introduce, user_type, is_deleted, deleted_at, is_email_agreed, last_login_at, created_at, updated_at) "
                    + "VALUES (2L, 'example2@example.com', 'example2', 1234, null, 'CREATOR', false, null, false, NOW(), NOW(), NOW());",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, thumbnail_id, is_show, created_at, updated_at, is_deleted) "
                    + "VALUES (1L, 1L, '파이썬 설치 강의', '설명', 20000, 0, 0, '파이썬 개발 환경 설치가 처음인 초보자', null, true, NOW(), NOW(), false);",
            "INSERT INTO web_service.courses (id, user_id, title, description, price, view_count, like_count, target_audience, thumbnail_id, is_show, created_at, updated_at, is_deleted) "
                    + "VALUES (2L, 2L, '자바 설치 강의', '다른 사람이 올린 영상', 20000, 0, 0, '자바 개발 환경 설치가 처음인 초보자', null, true, NOW(), NOW(), false);",
            "INSERT INTO web_service.tags (id, tag_name, created_at, updated_at) " +
                    "VALUES (1L, '존재하고 강의에 연결된 태그', NOW(), NOW())",
            "INSERT INTO web_service.tags (id, tag_name, created_at, updated_at) " +
                    "VALUES (2L, '존재하지만 강의에는 연결되지 않은 태그', NOW(), NOW())",
            "INSERT INTO web_service.course_tags (tag_id, course_id, created_at, updated_at) " +
                    "VALUES (1L, 1L, NOW(), NOW())"
    })
    @Test
    void searchMyCourse_정상() {
        // given
        Long userId = 1L;
        int page = 1;
        int pageSize = 10;
        CourseMySearchReq req = new CourseMySearchReq(page, pageSize);

        // when
        SearchRes<CourseMySearchInfo> res = courseService.searchMyCourse(userId, req);

        // then
        List<CourseMySearchInfo> items = res.items();
        PaginationRes pagination = res.pagination();

        assertThat(pagination).isNotNull();
        assertThat(pagination.totalItems()).isEqualTo(1);
        assertThat(pagination.totalPages()).isEqualTo(1);
        assertThat(pagination.currentPage()).isEqualTo(1);
        assertThat(pagination.perPage()).isEqualTo(10);

        assertThat(items).isNotNull();
        assertThat(items.size()).isEqualTo(1);
        assertThat(items.get(0).tags()).containsExactlyInAnyOrder("존재하고 강의에 연결된 태그");
    }
}