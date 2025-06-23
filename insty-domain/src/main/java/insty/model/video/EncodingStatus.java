package insty.model.video;

public enum EncodingStatus {
    WAITING,
    PROCESSING,
    COMPLETED,
    FAILED,
    FAILED_INVALID_VIDEO_LENGTH,
    FAILED_NOT_FOUND_VOICE
}
