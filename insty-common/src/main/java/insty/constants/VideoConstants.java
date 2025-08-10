package insty.constants;

import java.util.Set;

public class VideoConstants {

    public static final String DOMAIN = "Domain";
    public static final String PATH = "Path";
    public static final String PREVIEW_BASE_FOLDER = "preview";

    public static final long VIDEO_EXPIRATION_MINUTES = 360L;
    public static final long PREVIEW_VIDEO_EXPIRATION_MINUTES = 10L;

    public static final Set<String> ALLOWED_THUMBNAIL_TYPES = Set.of("image/jpeg", "image/png");
}
