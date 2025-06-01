package insty.error;

public enum CommonErrorCode implements ErrorCode {
    INVALID_INPUT("COMMON_001", "입력 데이터가 유효하지 않습니다.", 400),
    INVALID_PAGINATION_REQUEST("COMMON_002", "페이지네이션 데이터가 잘못되었습니다.", 400),
    INVALID_SORT_REQUEST("COMMON_003", "정렬 요청이 잘못되었습니다.", 400),
    UNAUTHORIZED("COMMON_004", "인증이 필요합니다.", 401),
    FORBIDDEN("COMMON_005", "해당 작업을 수행할 권한이 없습니다.", 403),
    RESOURCE_NOT_FOUND("COMMON_006", "요청한 리소스를 찾을 수 없습니다.", 404),
    API_NOT_FOUND("COMMON_007", "존재하지 않는 API입니다.", 404),
    CONFLICT("COMMON_008", "리소스 충돌이 발생했습니다.", 409),
    REQUEST_TOO_LARGE("COMMON_009", "요청/파일 크기가 너무 큽니다.", 413),
    UNSUPPORTED_MEDIA_TYPE("COMMON_010", "잘못된 Content-Type으로 요청하였습니다.", 415),
    PARAMETER_VALIDATION_ERROR("COMMON_011", "파라미터 검증에 실패했습니다.", 422),
    BAD_REQUEST_BODY("COMMON_012", "요청 형식이 잘못되었습니다.", 422),
    INVALID_TYPE_PARAMETER("COMMON_013", "파라미터 타입이 유효하지 않습니다.", 422),
    INTERNAL_ERROR("COMMON_014", "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.", 500),
    DOCUMENTATION_ONLY("COMMON_099", "문서 생성을 위한 목업 API 입니다.", 501);

    private final String code;
    private final String message;
    private final int httpCode;

    CommonErrorCode(String code, String message, int httpCode) {
        this.code = code;
        this.message = message;
        this.httpCode = httpCode;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public int getHttpCode() {
        return httpCode;
    }
}
