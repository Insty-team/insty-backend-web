package insty.model.community;

import insty.model.BaseEntity;
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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private CommunityQuestion communityQuestion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", nullable = false)
    private File file;

    @CreatedDate
    @Column(nullable = false, name = "created_at", updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false, name = "updated_at", updatable = false)
    private Instant updatedAt;


    public static CommunityFile create(CommunityQuestion communityQuestion, File file) {
        return CommunityFile.builder()
                .communityQuestion(communityQuestion)
                .file(file)
                .build();
    }
}
