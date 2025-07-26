package insty.domain.video.strategy;

public interface VideoValidateStrategy {
    void validateUploadable(Long userId);

    void validateReadable(Long userId, Long videoId);

    void verifyEncodingCompletedAndDeleted(Long parentId);
}
