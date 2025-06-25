package insty.model.community;

import insty.model.BaseEntity;
import insty.model.community.id.CommunityFileId;
import insty.model.file.File;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;

@Entity
@Table(name = "community_attachments", schema = "web_service")
@Getter
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class CommunityFile extends BaseEntity {

    @EmbeddedId
    private CommunityFileId communityFileId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private CommunityQuestion communityQuestion;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST, orphanRemoval = true)
    @JoinColumn(name = "file_id", nullable = false)
    private File file;

    public static CommunityFile create(CommunityQuestion communityQuestion, File file) {
        return CommunityFile.builder()
                .communityQuestion(communityQuestion)
                .file(file)
                .build();
    }
}
