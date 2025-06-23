package insty.global.advice;

import insty.error.CommonErrorCode;
import insty.error.ErrorCode;
import insty.exception.CustomException;
import insty.global.response.ErrorInfo;
import insty.global.response.FailRes;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice
public class ExceptionAdvice {

    /**
     * 커스텀 예외
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<FailRes<?>> handleCustomExceptions(CustomException e) {
        ErrorCode errorCode = e.getErrorCode();

        log.warn("[CUSTOM EXCEPTION] code: [{}], message: [{}]",
                errorCode.getCode(),
                errorCode.getMessage());

        FailRes<?> body = FailRes.of(ErrorInfo.of(errorCode));
        HttpStatus httpStatus = HttpStatus.valueOf(errorCode.getHttpCode());
        return new ResponseEntity<>(body, httpStatus);
    }

    /**
     * 사용자 인증이 되질 않음 - 401
     */
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public FailRes<?> handleInvalidPathExceptions(AuthenticationException e) {
        return FailRes.of(ErrorInfo.of(CommonErrorCode.UNAUTHORIZED));
    }

    /**
     * 작업을 수행할 권한이 없음 - 403
     */
    @ExceptionHandler(AuthorizationDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public FailRes<?> handleInvalidPathExceptions(AuthorizationDeniedException e) {
        return FailRes.of(ErrorInfo.of(CommonErrorCode.FORBIDDEN));
    }

    /**
     * 경로 존재하지 않음 - 404
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public FailRes<?> handleInvalidPathExceptions(NoHandlerFoundException e) {
        return FailRes.of(ErrorInfo.of(CommonErrorCode.API_NOT_FOUND));
    }

    /**
     * 리소스 충돌 - 409
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public FailRes<?> handleDataIntegrityViolationExceptions(DataIntegrityViolationException e) {
        return FailRes.of(ErrorInfo.of(CommonErrorCode.CONFLICT));
    }

    /**
     * 요청/파일 크기 제한 - 413
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public FailRes<?> handleMaxUploadSizeExceededExceptions(MaxUploadSizeExceededException e) {
        return FailRes.of(ErrorInfo.of(CommonErrorCode.REQUEST_TOO_LARGE));
    }

    /**
     * 파라미터 검증 예외 - 422
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public FailRes<List<ParameterData>> handleValidationExceptions(MethodArgumentNotValidException e) {
        log.warn("[PARAMETER VALIDATION EXCEPTION] class: [{}], message: [{}], localizedMessage: [{}]",
                e.getClass().getSimpleName(),
                e.getMessage(),
                e.getLocalizedMessage());

        List<ParameterData> list = new ArrayList<>();

        BindingResult bindingResult = e.getBindingResult();
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            ParameterData parameterData = ParameterData.of(
                    fieldError.getField(),
                    fieldError.getRejectedValue() == null ? null : fieldError.getRejectedValue().toString(),
                    fieldError.getDefaultMessage()
            );
            list.add(parameterData);
        }

        return FailRes.of(ErrorInfo.ofWithDetails(CommonErrorCode.PARAMETER_VALIDATION_ERROR, list));
    }

    /**
     * 요청 형식 예외 - 422
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public FailRes<?> handleHttpMessageParsingExceptions(HttpMessageNotReadableException e) {
        return FailRes.of(ErrorInfo.ofWithDetails(CommonErrorCode.BAD_REQUEST_BODY, e.getMessage()));
    }

    /**
     * 매개변수 타입 예외 - 422
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public FailRes<?> handleHttpMessageParsingExceptions(MethodArgumentTypeMismatchException e) {
        return FailRes.of(ErrorInfo.ofWithDetails(CommonErrorCode.INVALID_TYPE_PARAMETER, e.getMessage()));
    }

    /**
     * 등록되지 않은 예외 - 500
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    protected FailRes<?> handleUntrackedException(Exception e) {
        log.error("[UNTRACKED ERROR] class: [{}], message: [{}]",
                e.getClass().getSimpleName(),
                e.getMessage());

        return FailRes.of(ErrorInfo.ofWithDetails(CommonErrorCode.INTERNAL_ERROR, e.getMessage()));
    }
}
