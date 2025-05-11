package insty.global.swagger;

import insty.error.ErrorCode;
import insty.global.error.CommonErrorCode;
import insty.global.error.ExampleErrorCode;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Getter;

@Getter
public enum SwaggerResponseDescription {

    EXAMPLE_SEARCH(new LinkedHashSet<>(Set.of(
    )));

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
