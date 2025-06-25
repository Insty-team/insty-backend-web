package insty.model.community.id;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CommunityFileId implements Serializable {

    private Long questionId;
    private Long fileId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CommunityFileId)) return false;

        CommunityFileId that = (CommunityFileId) o;
        return Objects.equals(questionId, that.questionId) &&
                Objects.equals(fileId, that.fileId);

    }

    @Override
    public int hashCode() {
        return Objects.hash(questionId, fileId);
    }

    public static CommunityFileId create(Long questionId, Long fileId) {
        return CommunityFileId.builder()
                .questionId(questionId)
                .fileId(fileId)
                .build();
    }
}

