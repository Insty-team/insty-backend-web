package insty.domain.course.service;

import static org.assertj.core.api.Assertions.assertThat;

import insty.cloudfront.adapter.CloudFrontSigner;
import insty.domain.course.dto.CourseInstallEnvChecklistInfo;
import insty.domain.course.dto.CoursePostReq;
import insty.domain.course.dto.CoursePostRes;
import insty.domain.course.implement.CourseReader;
import insty.domain.course.implement.CourseWriter;
import insty.domain.course.repository.CourseInstallEnvChecklistRepository;
import insty.domain.course.repository.CourseKeypointRepository;
import insty.domain.course.repository.CourseRepository;
import insty.domain.course.repository.CourseTagRepository;
import insty.domain.tag.implement.TagWriter;
import insty.domain.tag.repository.TagsRepository;
import insty.global.property.AppProperties;
import insty.s3.adapter.S3UrlIssuer;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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
}