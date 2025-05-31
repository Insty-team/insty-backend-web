package insty.global.swagger;

import static insty.error.VideoErrorCode.VIDEO_CONTENT_TYPE_ERROR;
import static insty.error.VideoErrorCode.VIDEO_INVALID_FILE_NAME;
import static insty.error.VideoErrorCode.VIDEO_TYPE_NOT_MATCH;

import insty.error.CommonErrorCode;
import insty.error.ErrorCode;
import insty.error.ExampleErrorCode;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Getter;

@Getter
public enum SwaggerResponseDescription {

    EXAMPLE_SEARCH(new LinkedHashSet<>(Set.of(
    ))),

    // user
    USER_INFO(new LinkedHashSet<>(Set.of())),
    // video
    VIDEO_UPLOAD(new LinkedHashSet<>(Set.of(
            VIDEO_CONTENT_TYPE_ERROR,
            VIDEO_INVALID_FILE_NAME,
            VIDEO_TYPE_NOT_MATCH
    ))),
    VIDEO_GET(new LinkedHashSet<>(Set.of(
    ))),
    // course
    COURSE_CREATE(new LinkedHashSet<>(Set.of())),
    COURSE_UPDATE(new LinkedHashSet<>(Set.of())),
    ;

    private Set<ErrorCode> errorCodeList;

    SwaggerResponseDescription(Set<ErrorCode> errorCodeList) {
        // 공통 에러
        errorCodeList.addAll(new LinkedHashSet<>(Set.of(
                CommonErrorCode.RESOURCE_NOT_FOUND,
                CommonErrorCode.CONFLICT,
                CommonErrorCode.PARAMETER_VALIDATION_ERROR,
                CommonErrorCode.BAD_REQUEST_BODY,
                CommonErrorCode.INVALID_TYPE_PARAMETER,
                CommonErrorCode.INTERNAL_ERROR
        )));

        if (this.name().startsWith("EXAMPLE_")) {
            errorCodeList.add(ExampleErrorCode.EXAMPLE_ERROR_CODE);
        }

        this.errorCodeList = errorCodeList;
    }
}
