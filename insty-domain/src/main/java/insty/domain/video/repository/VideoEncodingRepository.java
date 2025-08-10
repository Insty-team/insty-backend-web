package insty.domain.video.repository;

import insty.model.video.VideoEncoding;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoEncodingRepository extends JpaRepository<VideoEncoding, Long> {

    Optional<String> findEncodingS3KeyByVideoUuid(UUID videoUuid);

    Optional<VideoEncoding> findByVideoUuid(UUID videoUuid);

    List<VideoEncoding> findAllByVideoUuidIn(List<UUID> videoUuids);
}
