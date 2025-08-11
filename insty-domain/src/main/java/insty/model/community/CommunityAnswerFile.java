package insty.model.community;

import insty.error.CommunityErrorCode;
import insty.exception.CustomException;
import insty.model.BaseEntity;
import insty.model.community.id.CommunityAnswerFileId;
import insty.model.file.File;
import jakarta.persistence.CascadeType;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Entity
@Table(name = "community_answers_files", schema = "web_service")
@Getter
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class CommunityAnswerFile extends BaseEntity {

    @EmbeddedId
    private CommunityAnswerFileId communityAnswerFileId;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("answerId")
    @JoinColumn(name = "answer_id", nullable = false)
    private CommunityAnswer communityAnswer;

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true)
    @MapsId("fileId")
    @JoinColumn(name = "file_id", nullable = false)
    private File file;

    public static CommunityAnswerFile create(CommunityAnswer communityAnswer, File file) {
        validateCreate(communityAnswer, file);
        return CommunityAnswerFile.builder()
                .communityAnswerFileId(CommunityAnswerFileId.create(communityAnswer.getId(), file.getId()))
                .communityAnswer(communityAnswer)
                .file(file)
                .build();
    }

    private static void validateCreate(CommunityAnswer communityAnswer, File file) {
        if (communityAnswer == null) {
            log.error("생성 오류 - communityAnswer : null");
            throw new CustomException(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
        }
        if (file == null) {
            log.error("생성 오류 - file : null");
            throw new CustomException(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
        }
    }
}
