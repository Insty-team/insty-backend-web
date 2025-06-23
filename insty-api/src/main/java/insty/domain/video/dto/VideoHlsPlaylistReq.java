package insty.domain.video.dto;

import insty.model.video.VideoType;
import jakarta.validation.constraints.NotNull;

public record VideoHlsPlaylistReq(
        @NotNull
        VideoType type,
        @NotNull
        Long id
) {
}
