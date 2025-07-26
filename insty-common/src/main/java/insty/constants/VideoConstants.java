package insty.constants;

import java.util.Set;

public class VideoConstants {

    public static final String DOMAIN = "Domain";
    public static final String PATH = "Path";
    public static final int VIDEO_COURSE_UPLOAD_MINUTES_LIMIT = 30;
    public static final int VIDEO_QUESTION_UPLOAD_MINUTES_LIMIT = 5;
    public static final int VIDEO_ANSWER_UPLOAD_MINUTES_LIMIT = 5;
    public static final String PREVIEW_BASE_FOLDER = "preview";

    public static final long VIDEO_EXPIRATION_MINUTES = 360L;
    public static final long PREVIEW_VIDEO_EXPIRATION_MINUTES = 10L;

    public static final Set<String> ALLOWED_THUMBNAIL_TYPES = Set.of("image/jpeg", "image/png");
}
