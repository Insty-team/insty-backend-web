package insty.error;

public enum CommunityErrorCode implements ErrorCode {

    // Not Found Errors (404)
    COURSE_QUESTION_NOT_FOUND("COURSE_001", "질문을 찾을 수 없습니다.", 404),
    COURSE_ANSWER_NOT_FOUND("COURSE_002", "답변을 찾을 수 없습니다.", 404),

    // Server Errors (500)
    COURSE_CREATE_ERROR("COURSE_003", "커뮤니티 생성에 실패했습니다.", 500),
    COURSE_UPDATE_ERROR("COURSE_004", "커뮤니티 수정에 실패했습니다.", 500),
    COURSE_DELETE_ERROR("COURSE_005", "커뮤니티 삭제에 실패했습니다.", 500),

    // Bad Request Errors (400)
    COURSE_INVALID_VIDEO_UUID("COURSE_006", "잘못된 비디오 UUID 형식입니다.", 400),
    COURSE_ANSWER_NOT_BELONG_TO_QUESTION("COURSE_007", "해당 질문에 속하지 않는 답변입니다.", 400),
    COURSE_TITLE_IS_REQUIRED("COURSE_008", "제목을 입력해 주세요.", 400),
    COURSE_CONTENT_IS_REQUIRED("COURSE_009", "내용을 입력해 주세요.", 400),
    COURSE_COURSE_ID_IS_REQUIRED("COURSE_010", "강의 ID가 필요합니다.", 400),
    COURSE_USER_ID_IS_REQUIRED("COURSE_011", "사용자 ID가 필요합니다.", 400),
    COURSE_QUESTION_ID_IS_REQUIRED("COURSE_012", "질문 ID가 필요합니다.", 400),
    COURSE_ANSWER_ID_IS_REQUIRED("COURSE_013", "답변 ID가 필요합니다.", 400),
    COURSE_FILE_IS_EMPTY("COURSE_014", "파일이 비어있습니다.", 400),
    COURSE_ANSWER_INVALID_USER_ID("COURSE_026", "유효하지 않은 사용자입니다.", 400),
    COURSE_MAX_FILE_COUNT_EXCEEDED("COURSE_016", "첨부파일 개수가 초과되었습니다.", 400),
    COURSE_INVALID_VIDEO_OPERATION("COURSE_017", "잘못된 비디오 작업입니다.", 400),
    COURSE_FILE_SIZE_EXCEEDED("COURSE_018", "파일 크기가 제한을 초과했습니다.", 400),
    COURSE_INVALID_FILE_EXTENSION("COURSE_019", "지원하지 않는 파일 확장자입니다.", 400),

    // Forbidden Errors (403)
    COURSE_ANSWER_ACCEPT_PERMISSION_DENIED("COURSE_020", "질문 작성자만 답변을 채택할 수 있습니다.", 403),
    COURSE_NOT_QUESTION_AUTHOR("COURSE_021", "질문 작성자가 아닙니다.", 403),
    COURSE_NOT_ANSWER_AUTHOR("COURSE_022", "답변 작성자가 아닙니다.", 403),

    // Conflict Errors (409)
    COURSE_ALREADY_ACCEPTED_ANSWER("COURSE_023", "이미 채택된 답변이 존재합니다.", 409),
    COURSE_QUESTION_ALREADY_DELETED("COURSE_024", "이미 삭제된 질문입니다.", 409),
    COURSE_ANSWER_ALREADY_DELETED("COURSE_025", "이미 삭제된 답변입니다.", 409),

    ;


    private final String code;
    private final String message;
    private final int httpCode;

    CommunityErrorCode(String code, String message, int httpCode) {
        this.code = code;
        this.message = message;
        this.httpCode = httpCode; // Default HTTP code for not found
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
