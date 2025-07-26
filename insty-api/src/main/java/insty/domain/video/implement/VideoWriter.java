package insty.domain.video.implement;

import insty.domain.video.dto.VideoUploadReq;
import insty.domain.video.repository.VideoAnswerRepository;
import insty.domain.video.repository.VideoCourseRepository;
import insty.domain.video.repository.VideoQuestionRepository;
import insty.model.user.User;
import insty.model.video.VideoAnswer;
import insty.model.video.VideoCourse;
import insty.model.video.VideoQuestion;
import insty.uuid.UuidProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class VideoWriter {

    private final VideoCourseRepository videoCourseRepository;
    private final VideoQuestionRepository videoQuestionRepository;
    private final VideoAnswerRepository videoAnswerRepository;
    private final UuidProvider uuidProvider;

    public VideoCourse saveVideoCourse(VideoUploadReq req, User user) {
        VideoCourse videoCourse = VideoCourse.create(req.fileName(), uuidProvider.generate(), user);
        return videoCourseRepository.save(videoCourse);
    }

    public VideoQuestion saveVideoQuestion(VideoUploadReq req, User user) {
        VideoQuestion videoQuestion = VideoQuestion.create(req.fileName(), uuidProvider.generate(), user);
        return videoQuestionRepository.save(videoQuestion);
    }

    public VideoAnswer saveVideoAnswer(VideoUploadReq req, User user) {
        VideoAnswer videoAnswer = VideoAnswer.create(req.fileName(), uuidProvider.generate(), user);
        return videoAnswerRepository.save(videoAnswer);
    }
}
