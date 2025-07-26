package insty.domain.video.strategy;

import insty.domain.video.dto.VideoUploadReq;
import insty.model.user.User;
import insty.model.video.BaseVideo;

public interface VideoWriteStrategy {
    BaseVideo saveVideo(VideoUploadReq req, User user);
}
