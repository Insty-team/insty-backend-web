package insty.domain.user.implement;

import insty.domain.common.FileCreateReq;
import insty.domain.file.implement.FileWriter;
import insty.domain.user.repository.UserRepository;
import insty.global.property.AppProperties;
import insty.model.file.File;
import insty.model.file.FileContainerType;
import insty.model.user.User;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
public class UserFileWriter {

    private final FileWriter fileWriter;
    private final AppProperties appProperties;
    private final UserRepository userRepository;

    /**
     *  프로필 이미지 저장
     */
    public Optional<String> saveProfileImageGetUrl(User user, MultipartFile profileImage) {
        if(profileImage == null || profileImage.isEmpty()) {
            return Optional.empty();
        }
        FileCreateReq req = new FileCreateReq(profileImage, FileContainerType.USER_PROFILEIMAGE, user.getId());
        File saveProfileImage = fileWriter.saveFile(req);
        user.updateProfileImage(saveProfileImage);
        userRepository.save(user);

        return Optional.ofNullable(saveProfileImage.getUrl(appProperties.getDomain()));
    }

}
