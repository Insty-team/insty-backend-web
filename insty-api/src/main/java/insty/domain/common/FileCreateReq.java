package insty.domain.common;

import insty.error.CommonErrorCode;
import insty.exception.CustomException;
import insty.model.file.FileContainerType;
import org.springframework.web.multipart.MultipartFile;

public record FileCreateReq(
        MultipartFile file,
        FileContainerType containerType,
        Long containerId
) {

    public FileCreateReq {
        if (file.isEmpty() || containerType == null || containerId == null) {
            throw new CustomException(CommonErrorCode.INVALID_FILE_CREATE_REQUEST);
        }
    }
}
