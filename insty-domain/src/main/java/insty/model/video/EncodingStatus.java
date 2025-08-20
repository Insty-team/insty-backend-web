package insty.model.video;

import java.util.List;

public enum EncodingStatus {
    WAITING,
    PROCESSING,
    COMPLETED,
    FAILED,
    FAILED_INVALID_VIDEO_LENGTH,
    FAILED_NOT_FOUND_VOICE;

    public static List<EncodingStatus> getExceedUploadLimitTarget() {
        return List.of(WAITING, PROCESSING, COMPLETED);
    }
}
