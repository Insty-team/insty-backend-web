package insty.domain.video.strategy;

public interface VideoValidateStrategy {
    void validateUploadable(Long userId);

    // TODO - 구매한 사람인지 추가 검증
    void validateReadable(Long userId, Long videoId);

    /**
     * 인코딩이 완료된 영상만 조회할 수 있다.<br> 가상 삭제에 주의한다. VideoCourse - 가상 삭제 없음
     *
     * @param parentId courseId/communityQuestionId/communityAnswerId
     */
    void verifyEncodingCompletedAndDeleted(Long parentId);
}
