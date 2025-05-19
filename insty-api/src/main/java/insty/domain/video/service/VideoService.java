package insty.domain.video.service;

import insty.domain.video.dto.VideoUploadReq;
import insty.domain.video.dto.VideoUploadRes;
import insty.domain.video.implement.VideoValidator;
import insty.domain.video.implement.VideoWriter;
import insty.model.video.Video;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class VideoService {

    private final VideoValidator videoValidator;
    private final VideoWriter videoWriter;

    public VideoUploadRes getPreSignedURLForUpload(VideoUploadReq req) {
        videoValidator.validateContentType(req.fileName(), req.contentType());
        videoValidator.validateUploadable(); // TODO - 메서드 구현

        Video video = videoWriter.save(req);
        // TODO - pre-signed url 발급
        // TODO - aws 람다에 인코딩 완료 시 db 컬럼 상태값 업데이트
        return new VideoUploadRes(video.getVideoUuid(), null, null);
    }
}
