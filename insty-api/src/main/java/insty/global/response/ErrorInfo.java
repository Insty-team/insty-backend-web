package insty.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import insty.error.ErrorCode;

public record ErrorInfo<T>(
        String code,
        String message,
        @JsonInclude(JsonInclude.Include.NON_NULL) T details
) {

    private static <T> ErrorInfo<T> createErrorInfo(ErrorCode errorCode, String customMessage, T details) {
        return new ErrorInfo<>(
                errorCode.getCode(),
                customMessage != null ? customMessage : errorCode.getMessage(),
                details
        );
    }

    public static <T> ErrorInfo<T> of(ErrorCode errorCode) {
        return ErrorInfo.createErrorInfo(errorCode, null, null);
    }

    public static <T> ErrorInfo<T> of(ErrorCode errorCode, String customMessage) {
        return ErrorInfo.createErrorInfo(errorCode, customMessage, null);
    }

    public static <T> ErrorInfo<T> ofWithDetails(ErrorCode errorCode, T details) {
        return ErrorInfo.createErrorInfo(errorCode, null, details);
    }

    public static <T> ErrorInfo<T> ofWithDetails(ErrorCode errorCode, T details, String customMessage) {
        return ErrorInfo.createErrorInfo(errorCode, customMessage, details);
    }
}
