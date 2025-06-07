package insty.domain.user.implement;

import insty.global.property.AppProperties;
import insty.model.file.File;
import insty.model.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserFileReader {
    private final AppProperties appProperties;

    /**
     *  사용자 프로필 이미지 조회
     */
    public String getProfileImageUrl(User user) {
        File profileImage = user.getProfileImage();
        if (profileImage != null) {
            return profileImage.getUrl(appProperties.getDomain());
        }
        return null;
    }
}
