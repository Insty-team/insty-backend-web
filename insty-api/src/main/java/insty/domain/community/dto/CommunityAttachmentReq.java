package insty.domain.community.dto;

import insty.model.file.FileContainerType;
import jakarta.validation.constraints.NotNull;

public record CommunityAttachmentReq(
        @NotNull
        Long questionId,
        @NotNull
        Long fileId,
        @NotNull
        FileContainerType fileContainerType,
        @NotNull
        String contentType,
        @NotNull
        String fileContent
) {
    public static CommunityAttachmentReq create(
            Long questionId,
            Long fileId,
            FileContainerType fileContainerType,
            String contentType,
            String fileContent
    ) {
        return new CommunityAttachmentReq(questionId, fileId, fileContainerType, contentType, fileContent);
    }
}
