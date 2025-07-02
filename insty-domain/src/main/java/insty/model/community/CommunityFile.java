package insty.model.community;

import insty.error.CommunityErrorCode;
import insty.error.CourseErrorCode;
import insty.exception.CustomException;
import insty.model.BaseEntity;
import insty.model.community.id.CommunityFileId;
import insty.model.course.Course;
import insty.model.file.File;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;

@Slf4j
@Entity
@Table(name = "community_attachments", schema = "web_service")
@Getter
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class CommunityFile extends BaseEntity {

    @EmbeddedId
    private CommunityFileId communityFileId;
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("questionId")
    @JoinColumn(name = "question_id", nullable = false)
    private CommunityQuestion communityQuestion;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST, orphanRemoval = true)
    @MapsId("fileId")
    @JoinColumn(name = "file_id", nullable = false)
    private File file;

    public static CommunityFile create(CommunityQuestion communityQuestion, File file) {
        validateCreate(communityQuestion, file);
        return CommunityFile.builder()
                .communityFileId(CommunityFileId.create(communityQuestion.getId(), file.getId()))
                .communityQuestion(communityQuestion)
                .file(file)
                .build();
    }

    private static void validateCreate(CommunityQuestion communityQuestion, File file) {
        if (communityQuestion == null) {
            log.error("생성 오류 - communityQuestion : null");
            throw new CustomException(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
        }
        if (file == null) {
            log.error("생성 오류 - file : null");
            throw new CustomException(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
        }
    }
}
