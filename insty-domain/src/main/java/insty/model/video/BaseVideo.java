package insty.model.video;

import java.util.UUID;

public interface BaseVideo {
    UUID getVideoUuid();

    String getS3Key();

    String getOriginalFileName();
}
