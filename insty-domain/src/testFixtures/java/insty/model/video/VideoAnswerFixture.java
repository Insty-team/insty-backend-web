package insty.model.video;

import insty.model.user.User;
import java.util.UUID;

public class VideoAnswerFixture {

    public static VideoAnswer getVideoAnswer(User user) {
        return VideoAnswer.create("fileName.mp4", UUID.fromString("00000000-0000-0000-0000-000000000001"), user);
    }
}
