package insty.domain.video.dto;

import insty.s3.dto.PresignedUrlDto;
import java.time.Instant;
import java.util.UUID;

public record VideoUploadRes(
        UUID uuid,
        String uploadUrl,
        Instant expiredAt
) {

    public static VideoUploadRes from(UUID uuid, PresignedUrlDto presignedUrlDto) {
        return new VideoUploadRes(uuid, presignedUrlDto.presignedUrl(), presignedUrlDto.expiredAt());
    }
}
