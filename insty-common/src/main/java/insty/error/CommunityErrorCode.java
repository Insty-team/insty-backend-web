package insty.error;

public enum CommunityErrorCode implements ErrorCode {

    // Not Found Errors (404)
    COMMUNITY_POST_NOT_FOUND("COMMUNITY_001", "커뮤니티 게시글을 찾을 수 없습니다.", 404),
    COMMUNITY_COMMENT_NOT_FOUND("COMMUNITY_002", "커뮤니티 댓글을 찾을 수 없습니다.", 404),

    // Server Errors (500)
    COMMUNITY_CREATE_ERROR("COMMUNITY_003", "커뮤니티 도메인 생성에 실패했습니다.", 500),
    COMMUNITY_UPDATE_ERROR("COMMUNITY_004", "커뮤니티 도메인 수정에 실패했습니다.", 500),
    COMMUNITY_DELETE_ERROR("COMMUNITY_005", "커뮤니티 도메인 삭제에 실패했습니다.", 500),

    // Bad Request Errors (400)
    COMMUNITY_TITLE_REQUIRED("COMMUNITY_006", "제목을 입력해 주세요.", 400),
    COMMUNITY_CONTENT_REQUIRED("COMMUNITY_007", "내용을 입력해 주세요.", 400),
    COMMUNITY_USER_ID_REQUIRED("COMMUNITY_008", "사용자 ID가 필요합니다.", 400),
    COMMUNITY_POST_ID_REQUIRED("COMMUNITY_009", "게시글 ID가 필요합니다.", 400),
    COMMUNITY_FILE_IS_EMPTY("COMMUNITY_010", "파일이 비어있습니다.", 400),
    COMMUNITY_MAX_FILE_COUNT_EXCEEDED("COMMUNITY_011", "첨부파일 개수가 초과되었습니다.", 400),

    // Conflict Errors (409)
    COMMUNITY_POST_ALREADY_DELETED("COMMUNITY_012", "이미 삭제된 게시글입니다.", 409),
    COMMUNITY_COMMENT_ALREADY_DELETED("COMMUNITY_013", "이미 삭제된 댓글입니다.", 409),

    // Forbidden Errors (403)
    COMMUNITY_NOT_POST_AUTHOR("COMMUNITY_014", "게시글 작성자가 아닙니다.", 403),
    COMMUNITY_NOT_COMMENT_AUTHOR("COMMUNITY_015", "댓글 작성자가 아닙니다.", 403);

    private final String code;
    private final String message;
    private final int httpCode;

    CommunityErrorCode(String code, String message, int httpCode) {
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
