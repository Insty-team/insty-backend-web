package insty.model.video;

import java.util.UUID;

public class VideoEncodingFixture {

    public static VideoEncoding getVideoEncoding() {
        return VideoEncoding.builder()
                .videoUuid(UUID.fromString("00000000-0000-0000-0000-000000000001"))
                .format("hls")
                .encodingS3Key("vod/COURSE/hls/00000000-0000-0000-0000-000000000001/fileName.mp4")
                .build();
    }
}
