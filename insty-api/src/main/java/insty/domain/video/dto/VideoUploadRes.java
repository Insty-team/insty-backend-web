package insty.domain.video.dto;

import java.time.Instant;
import java.util.UUID;

public record VideoUploadRes(
        UUID uuid,
        String uploadUrl,
        Instant expiredAt
) {
}
