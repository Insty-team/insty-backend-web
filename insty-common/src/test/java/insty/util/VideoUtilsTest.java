package insty.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class VideoUtilsTest {

    @Test
    void getVideoBasicThumbnailUrl_정상() {
        // given
        String domain = "insty.test.com";
        UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000001");

        // when
        String thumbnailUrl = VideoUtils.getVideoBasicThumbnailUrl(domain, uuid);

        // then
        assertThat(thumbnailUrl).isEqualTo(
                "https://insty.test.com/file/VIDEO_BASIC_THUMBNAIL/00000000-0000-0000-0000-000000000001/basic_thumbnail.0000000.jpg");
    }
}