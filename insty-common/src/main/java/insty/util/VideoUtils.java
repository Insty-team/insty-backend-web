package insty.util;

import java.util.UUID;

public class VideoUtils {

    /**
     * AI에서도 사용하므로, 상수 변경 시 AI에도 알려줘야 합니다.
     */
    private static final String VIDEO_BASIC_THUMBNAIL_DIRECTORY = "file/VIDEO_BASIC_THUMBNAIL";
    private static final String VIDEO_BASIC_THUMBNAIL_NAME = "basic_thumbnail.jpg";

    public static String getVideoBasicThumbnailUrl(String domain, UUID videoUuid) {
        return "https://" + domain + "/" + VIDEO_BASIC_THUMBNAIL_DIRECTORY + "/" + videoUuid + "/"
                + VIDEO_BASIC_THUMBNAIL_NAME;
    }

    public static String getVideoBasicThumbnailKey(UUID videoUuid) {
        return VIDEO_BASIC_THUMBNAIL_DIRECTORY + "/" + videoUuid + "/" + VIDEO_BASIC_THUMBNAIL_NAME;
    }
}
