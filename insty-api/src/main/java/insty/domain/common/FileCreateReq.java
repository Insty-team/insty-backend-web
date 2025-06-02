package insty.domain.common;

import insty.model.file.FileContainerType;
import org.springframework.web.multipart.MultipartFile;

public record FileCreateReq(
        MultipartFile file,
        FileContainerType containerType,
        Long containerId
) {
}
