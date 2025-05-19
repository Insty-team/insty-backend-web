package insty.domain.video.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record VideoUploadRes(
        UUID uuid,
        String uploadUrl,
        LocalDateTime expiredAt
) {
}
