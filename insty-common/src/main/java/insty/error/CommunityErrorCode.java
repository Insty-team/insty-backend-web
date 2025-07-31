package insty.error;

public enum CommunityErrorCode implements ErrorCode {

    // Not Found Errors (404)
    COMMUNITY_QUESTION_NOT_FOUND("COMMUNITY_001", "질문을 찾을 수 없습니다.", 404),
    COMMUNITY_ANSWER_NOT_FOUND("COMMUNITY_002", "답변을 찾을 수 없습니다.", 404),

    // Server Errors (500)
    COMMUNITY_CREATE_ERROR("COMMUNITY_003", "커뮤니티 생성에 실패했습니다.", 500),
    COMMUNITY_UPDATE_ERROR("COMMUNITY_004", "커뮤니티 수정에 실패했습니다.", 500),
    COMMUNITY_DELETE_ERROR("COMMUNITY_005", "커뮤니티 삭제에 실패했습니다.", 500),

    // Bad Request Errors (400)
    COMMUNITY_INVALID_VIDEO_UUID("COMMUNITY_006", "잘못된 비디오 UUID 형식입니다.", 400),
    COMMUNITY_ANSWER_NOT_BELONG_TO_QUESTION("COMMUNITY_007", "해당 질문에 속하지 않는 답변입니다.", 400),
    COMMUNITY_TITLE_IS_REQUIRED("COMMUNITY_008", "제목을 입력해 주세요.", 400),
    COMMUNITY_CONTENT_IS_REQUIRED("COMMUNITY_009", "내용을 입력해 주세요.", 400),
    COMMUNITY_COURSE_ID_IS_REQUIRED("COMMUNITY_010", "강의 ID가 필요합니다.", 400),
    COMMUNITY_USER_ID_IS_REQUIRED("COMMUNITY_011", "사용자 ID가 필요합니다.", 400),
    COMMUNITY_QUESTION_ID_IS_REQUIRED("COMMUNITY_012", "질문 ID가 필요합니다.", 400),
    COMMUNITY_ANSWER_ID_IS_REQUIRED("COMMUNITY_013", "답변 ID가 필요합니다.", 400),
    COMMUNITY_FILE_IS_EMPTY("COMMUNITY_014", "파일이 비어있습니다.", 400),
    COMMUNITY_ANSWER_USER_TYPE_INVALID("COMMUNITY_015", "CREATOR의 답변만 채택할 수 있습니다.", 400),
    COMMUNITY_MAX_FILE_COUNT_EXCEEDED("COMMUNITY_016", "첨부파일 개수가 초과되었습니다.", 400),
    COMMUNITY_INVALID_VIDEO_OPERATION("COMMUNITY_017", "잘못된 비디오 작업입니다.", 400),
    COMMUNITY_FILE_SIZE_EXCEEDED("COMMUNITY_018", "파일 크기가 제한을 초과했습니다.", 400),
    COMMUNITY_INVALID_FILE_EXTENSION("COMMUNITY_019", "지원하지 않는 파일 확장자입니다.", 400),

    // Forbidden Errors (403)
    COMMUNITY_ANSWER_ACCEPT_PERMISSION_DENIED("COMMUNITY_020", "질문 작성자만 답변을 채택할 수 있습니다.", 403),
    COMMUNITY_NOT_QUESTION_AUTHOR("COMMUNITY_021", "질문 작성자가 아닙니다.", 403),
    COMMUNITY_NOT_ANSWER_AUTHOR("COMMUNITY_022", "답변 작성자가 아닙니다.", 403),

    // Conflict Errors (409)
    COMMUNITY_ALREADY_ACCEPTED_ANSWER("COMMUNITY_023", "이미 채택된 답변이 존재합니다.", 409),
    COMMUNITY_QUESTION_ALREADY_DELETED("COMMUNITY_024", "이미 삭제된 질문입니다.", 409),
    COMMUNITY_ANSWER_ALREADY_DELETED("COMMUNITY_025", "이미 삭제된 답변입니다.", 409),

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
