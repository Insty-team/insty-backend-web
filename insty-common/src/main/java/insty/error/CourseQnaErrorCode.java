package insty.error;

public enum CourseQnaErrorCode implements ErrorCode {

    // Not Found Errors (404)
    COURSE_QNA_QUESTION_NOT_FOUND("COURSE_QNA_001", "질문을 찾을 수 없습니다.", 404),
    COURSE_QNA_ANSWER_NOT_FOUND("COURSE_QNA_002", "답변을 찾을 수 없습니다.", 404),

    // Server Errors (500)
    COURSE_QNA_CREATE_ERROR("COURSE_QNA_003", "강좌 QNA 생성에 실패했습니다.", 500),
    COURSE_QNA_UPDATE_ERROR("COURSE_QNA_004", "강좌 QNA 수정에 실패했습니다.", 500),
    COURSE_QNA_DELETE_ERROR("COURSE_QNA_005", "강좌 QNA 삭제에 실패했습니다.", 500),

    // Bad Request Errors (400)
    COURSE_QNA_INVALID_VIDEO_UUID("COURSE_QNA_006", "잘못된 비디오 UUID 형식입니다.", 400),
    COURSE_QNA_ANSWER_NOT_BELONG_TO_QUESTION("COURSE_QNA_007", "해당 질문에 속하지 않는 답변입니다.", 400),
    COURSE_QNA_TITLE_IS_REQUIRED("COURSE_QNA_008", "제목을 입력해 주세요.", 400),
    COURSE_QNA_CONTENT_IS_REQUIRED("COURSE_QNA_009", "내용을 입력해 주세요.", 400),
    COURSE_QNA_COURSE_ID_IS_REQUIRED("COURSE_QNA_010", "강의 ID가 필요합니다.", 400),
    COURSE_QNA_USER_ID_IS_REQUIRED("COURSE_QNA_011", "사용자 ID가 필요합니다.", 400),
    COURSE_QNA_QUESTION_ID_IS_REQUIRED("COURSE_QNA_012", "질문 ID가 필요합니다.", 400),
    COURSE_QNA_ANSWER_ID_IS_REQUIRED("COURSE_QNA_013", "답변 ID가 필요합니다.", 400),
    COURSE_QNA_FILE_IS_EMPTY("COURSE_QNA_014", "파일이 비어있습니다.", 400),
    COURSE_QNA_ANSWER_INVALID_USER_ID("COURSE_QNA_026", "유효하지 않은 사용자입니다.", 400),
    COURSE_QNA_MAX_FILE_COUNT_EXCEEDED("COURSE_QNA_016", "첨부파일 개수가 초과되었습니다.", 400),
    COURSE_QNA_INVALID_VIDEO_OPERATION("COURSE_QNA_017", "잘못된 비디오 작업입니다.", 400),
    COURSE_QNA_FILE_SIZE_EXCEEDED("COURSE_QNA_018", "파일 크기가 제한을 초과했습니다.", 400),
    COURSE_QNA_INVALID_FILE_EXTENSION("COURSE_QNA_019", "지원하지 않는 파일 확장자입니다.", 400),

    // Forbidden Errors (403)
    COURSE_QNA_ANSWER_ACCEPT_PERMISSION_DENIED("COURSE_QNA_020", "질문 작성자만 답변을 채택할 수 있습니다.", 403),
    COURSE_QNA_NOT_QUESTION_AUTHOR("COURSE_QNA_021", "질문 작성자가 아닙니다.", 403),
    COURSE_QNA_NOT_ANSWER_AUTHOR("COURSE_QNA_022", "답변 작성자가 아닙니다.", 403),

    // Conflict Errors (409)
    COURSE_QNA_ALREADY_ACCEPTED_ANSWER("COURSE_QNA_023", "이미 채택된 답변이 존재합니다.", 409),
    COURSE_QNA_QUESTION_ALREADY_DELETED("COURSE_QNA_024", "이미 삭제된 질문입니다.", 409),
    COURSE_QNA_ANSWER_ALREADY_DELETED("COURSE_QNA_025", "이미 삭제된 답변입니다.", 409);

    private final String code;
    private final String message;
    private final int httpCode;

    CourseQnaErrorCode(String code, String message, int httpCode) {
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
