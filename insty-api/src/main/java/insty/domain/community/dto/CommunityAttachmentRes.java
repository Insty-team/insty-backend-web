package insty.domain.community.dto;

import insty.model.file.FileContainerType;
import jakarta.validation.constraints.NotNull;

public record CommunityAttachmentRes(
        @NotNull
        FileContainerType fileContainerType,
        @NotNull
        String contentType,
        @NotNull
        String fileContent
) {

    public static CommunityAttachmentRes create(
            FileContainerType fileContainerType,
            String contentType,
            String fileContent
    ) {
        return new CommunityAttachmentRes(fileContainerType, contentType, fileContent);
    }
}
