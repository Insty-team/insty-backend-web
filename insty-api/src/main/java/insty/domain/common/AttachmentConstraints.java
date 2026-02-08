package insty.domain.common;

/**
 * 이미지/파일 첨부 개수와 관련된 계약을 한 곳에서 관리한다.
 * 현재 커뮤니티(게시글/댓글)와 강좌 Q&A(질문/답변) 모두 동일하게 2개까지 허용한다.
 */
public final class AttachmentConstraints {

    public static final int MAX_IMAGE_ATTACHMENTS = 2;

    // 도메인별 가독성을 위한 별칭
    public static final int MAX_POST_FILE_COUNT = MAX_IMAGE_ATTACHMENTS;
    public static final int MAX_COMMENT_FILE_COUNT = MAX_IMAGE_ATTACHMENTS;
    public static final int MAX_QNA_FILE_COUNT = MAX_IMAGE_ATTACHMENTS;

    // Swagger 문구와 동기화를 위한 설명 상수
    public static final String POST_ATTACHMENT_DESCRIPTION = "첨부파일 (최대 " + MAX_POST_FILE_COUNT + "개)";
    public static final String COMMENT_ATTACHMENT_DESCRIPTION = "첨부파일 (최대 " + MAX_COMMENT_FILE_COUNT + "개)";
    public static final String QNA_ATTACHMENT_DESCRIPTION = "첨부파일 (최대 " + MAX_QNA_FILE_COUNT + "개)";

    private AttachmentConstraints() {
    }
}
