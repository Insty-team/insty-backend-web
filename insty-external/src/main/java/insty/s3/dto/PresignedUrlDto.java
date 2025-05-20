package insty.s3.dto;

import java.time.Instant;

public record PresignedUrlDto(
        String presignedUrl,
        Instant expiredAt
) {
}
