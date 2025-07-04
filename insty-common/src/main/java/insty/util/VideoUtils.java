package insty.util;

import static insty.constants.VideoConstants.VIDEO_BASIC_THUMBNAIL_NAME;

import java.util.UUID;

public class VideoUtils {

    public static String getVideoBaseThumbnailUrl(String domain, UUID videoUuid) {
        return "https://" + domain + "/file/VIDEO_BASIC_THUMBNAIL/" + videoUuid + "/" + VIDEO_BASIC_THUMBNAIL_NAME;
    }

    public static String getVideoBaseThumbnailKey(UUID videoUuid) {
        return "file/VIDEO_BASIC_THUMBNAIL/" + videoUuid + "/" + VIDEO_BASIC_THUMBNAIL_NAME;
    }
}
