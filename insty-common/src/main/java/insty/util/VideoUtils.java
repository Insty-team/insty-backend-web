package insty.util;

import java.util.UUID;

public class VideoUtils {

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
