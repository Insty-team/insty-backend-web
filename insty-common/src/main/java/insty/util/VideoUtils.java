package insty.util;

import static insty.constants.VideoConstants.VIDEO_BASIC_THUMBNAIL_NAME;

import java.util.UUID;

public class VideoUtils {

    public static String getVideoBasicThumbnailUrl(String domain, UUID videoUuid) {
        return "https://" + domain + "/file/VIDEO_BASIC_THUMBNAIL/" + videoUuid + "/" + VIDEO_BASIC_THUMBNAIL_NAME;
    }

    public static String getVideoBasicThumbnailKey(UUID videoUuid) {
        return "file/VIDEO_BASIC_THUMBNAIL/" + videoUuid + "/" + VIDEO_BASIC_THUMBNAIL_NAME;
    }
}
