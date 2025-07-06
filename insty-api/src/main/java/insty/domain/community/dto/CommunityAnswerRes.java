package insty.domain.community.dto;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;

public record CommunityAnswerRes(
        @NotNull
        Long userId,
        @NotNull
        String content,
        String imageURL,
        Instant createdAt,
        Instant updatedAt,
        boolean isAccepted
) {
    public static CommunityAnswerRes create(
            @NotNull Long userId,
            @NotNull String content,
            String imageURL,
            Instant createdAt,
            Instant updatedAt,
            boolean isAccepted
    ) {
        return new CommunityAnswerRes(userId, content, imageURL, createdAt, updatedAt, isAccepted);
    }

}
