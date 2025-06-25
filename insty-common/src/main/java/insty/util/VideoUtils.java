package insty.util;

import java.util.UUID;

public class VideoUtils {

    public static String getVideoBaseThumbnailUrl(String domain, UUID videoUuid) {
        return "https://" + domain + "/file/VIDEO_BASIC_THUMBNAIL/" + videoUuid + "/basic_thumbnail.0000000.jpg";
    }
}
