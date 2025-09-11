package insty.global.swagger;

import static insty.ai.error.AiErrorCode.AI_API_REQUEST_FAILED;
import static insty.cloudfront.error.CloudFrontErrorCode.CLOUD_FRONT_GENERATE_PRESIGNED_URL_FAIL;
import static insty.cloudfront.error.CloudFrontErrorCode.CLOUD_FRONT_GENERATE_SIGNED_COOKIE_FAIL;
import static insty.error.CommunityErrorCode.COMMUNITY_ALREADY_ACCEPTED_ANSWER;
import static insty.error.CommunityErrorCode.COMMUNITY_ANSWER_ACCEPT_PERMISSION_DENIED;
import static insty.error.CommunityErrorCode.COMMUNITY_ANSWER_ALREADY_DELETED;
import static insty.error.CommunityErrorCode.COMMUNITY_ANSWER_ID_IS_REQUIRED;
import static insty.error.CommunityErrorCode.COMMUNITY_ANSWER_NOT_BELONG_TO_QUESTION;
import static insty.error.CommunityErrorCode.COMMUNITY_ANSWER_NOT_FOUND;
import static insty.error.CommunityErrorCode.COMMUNITY_ANSWER_INVALID_USER_ID;
import static insty.error.CommunityErrorCode.COMMUNITY_CONTENT_IS_REQUIRED;
import static insty.error.CommunityErrorCode.COMMUNITY_FILE_IS_EMPTY;
import static insty.error.CommunityErrorCode.COMMUNITY_MAX_FILE_COUNT_EXCEEDED;
import static insty.error.CommunityErrorCode.COMMUNITY_NOT_ANSWER_AUTHOR;
import static insty.error.CommunityErrorCode.COMMUNITY_NOT_QUESTION_AUTHOR;
import static insty.error.CommunityErrorCode.COMMUNITY_QUESTION_ALREADY_DELETED;
import static insty.error.CommunityErrorCode.COMMUNITY_QUESTION_ID_IS_REQUIRED;
import static insty.error.CommunityErrorCode.COMMUNITY_QUESTION_NOT_FOUND;
import static insty.error.CourseErrorCode.COURSE_CANT_CHANGE;
import static insty.error.CourseErrorCode.COURSE_NOT_FOUND;
import static insty.error.CourseErrorCode.COURSE_NOT_FOUND_LINKED_VIDEO;
import static insty.error.CourseErrorCode.COURSE_THUMBNAIL_INVALID_EXTENSION;
import static insty.error.CourseErrorCode.COURSE_TOO_MANY_PRACTICE_FILE;
import static insty.error.UserErrorCode.USER_NOT_FOUND;
import static insty.error.VideoErrorCode.VIDEO_BASIC_THUMBNAIL_NOT_FOUND;
import static insty.error.VideoErrorCode.VIDEO_CANT_READ;
import static insty.error.VideoErrorCode.VIDEO_CONTENT_TYPE_ERROR;
import static insty.error.VideoErrorCode.VIDEO_ENCODING_FAILED;
import static insty.error.VideoErrorCode.VIDEO_ENCODING_FAILED_INVALID_LENGTH;
import static insty.error.VideoErrorCode.VIDEO_ENCODING_FAILED_NOT_FOUND_VOICE;
import static insty.error.VideoErrorCode.VIDEO_EXCEED_UPLOAD_LIMIT;
import static insty.error.VideoErrorCode.VIDEO_INVALID_FILE_NAME;
import static insty.error.VideoErrorCode.VIDEO_NOT_FINISHED_ENCODING;
import static insty.error.VideoErrorCode.VIDEO_NOT_FOUND;
import static insty.error.VideoErrorCode.VIDEO_TYPE_NOT_MATCH;
import static insty.s3.error.S3ErrorCode.S3_HEAD_ERROR;

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
    USER_CREATE(new LinkedHashSet<>(Set.of())),
    USER_UPDATE(new LinkedHashSet<>(Set.of(
            USER_NOT_FOUND
    ))),
    USER_DETAIL(new LinkedHashSet<>(Set.of(
            USER_NOT_FOUND
    ))),
    USER_DELETE(new LinkedHashSet<>(Set.of(
            USER_NOT_FOUND
    ))),
    // video
    VIDEO_UPLOAD(new LinkedHashSet<>(Set.of(
            VIDEO_CONTENT_TYPE_ERROR,
            VIDEO_INVALID_FILE_NAME,
            VIDEO_TYPE_NOT_MATCH,
            VIDEO_EXCEED_UPLOAD_LIMIT,
            USER_NOT_FOUND
    ))),
    VIDEO_THUMBNAIL_GET(new LinkedHashSet<>(Set.of(
            VIDEO_BASIC_THUMBNAIL_NOT_FOUND,
            S3_HEAD_ERROR
    ))),
    VIDEO_GET(new LinkedHashSet<>(Set.of(
            VIDEO_NOT_FOUND,
            VIDEO_CANT_READ,
            VIDEO_ENCODING_FAILED,
            VIDEO_ENCODING_FAILED_INVALID_LENGTH,
            VIDEO_ENCODING_FAILED_NOT_FOUND_VOICE,
            VIDEO_NOT_FINISHED_ENCODING,
            CLOUD_FRONT_GENERATE_SIGNED_COOKIE_FAIL
    ))),
    VIDEO_PREVIEW(new LinkedHashSet<>(Set.of(
            VIDEO_NOT_FOUND,
            VIDEO_ENCODING_FAILED,
            VIDEO_ENCODING_FAILED_INVALID_LENGTH,
            VIDEO_ENCODING_FAILED_NOT_FOUND_VOICE,
            VIDEO_NOT_FINISHED_ENCODING,
            CLOUD_FRONT_GENERATE_PRESIGNED_URL_FAIL
    ))),
    // course
    COURSE_CREATE(new LinkedHashSet<>(Set.of(
            COURSE_THUMBNAIL_INVALID_EXTENSION,
            USER_NOT_FOUND,
            VIDEO_NOT_FOUND,
            COURSE_NOT_FOUND_LINKED_VIDEO
    ))),
    COURSE_UPDATE(new LinkedHashSet<>(Set.of(
            COURSE_NOT_FOUND,
            COURSE_CANT_CHANGE,
            COURSE_THUMBNAIL_INVALID_EXTENSION,
            VIDEO_NOT_FOUND,
            COURSE_NOT_FOUND_LINKED_VIDEO,
            COURSE_TOO_MANY_PRACTICE_FILE,
            AI_API_REQUEST_FAILED,
            VIDEO_NOT_FINISHED_ENCODING
    ))),
    COURSE_DELETE(new LinkedHashSet<>(Set.of(
            COURSE_NOT_FOUND,
            COURSE_CANT_CHANGE,
            VIDEO_NOT_FOUND,
            COURSE_NOT_FOUND_LINKED_VIDEO,
            AI_API_REQUEST_FAILED,
            VIDEO_NOT_FINISHED_ENCODING
    ))),
    COURSE_DETAIL(new LinkedHashSet<>(Set.of(
            COURSE_NOT_FOUND
    ))),
    COURSE_SEARCH(new LinkedHashSet<>(Set.of(
    ))),
    COURSE_MY_SEARCH(new LinkedHashSet<>(Set.of(
    ))),
    COURSE_REQUEST(new LinkedHashSet<>(Set.of(
    ))),
    //community
    COMMUNITY_QUESTION_SEARCH(new LinkedHashSet<>(Set.of(
    ))),
    COMMUNITY_QUESTION_MY_SEARCH(new LinkedHashSet<>(Set.of(
    ))),
    COMMUNITY_QUESTION_COURSE_SEARCH(new LinkedHashSet<>(Set.of(
    ))),
    COMMUNITY_QUESTION_DETAIL(new LinkedHashSet<>(Set.of(
            COMMUNITY_QUESTION_ID_IS_REQUIRED,
            COMMUNITY_QUESTION_NOT_FOUND,
            COMMUNITY_QUESTION_ALREADY_DELETED
    ))),
    COMMUNITY_QUESTION_CREATE(new LinkedHashSet<>(Set.of(
            USER_NOT_FOUND,
            COURSE_NOT_FOUND,
            VIDEO_NOT_FOUND,
            COMMUNITY_CONTENT_IS_REQUIRED,
            COMMUNITY_FILE_IS_EMPTY
    ))),
    COMMUNITY_QUESTION_UPDATE(new LinkedHashSet<>(Set.of(
            COMMUNITY_QUESTION_ID_IS_REQUIRED,
            COMMUNITY_QUESTION_NOT_FOUND,
            COMMUNITY_QUESTION_ALREADY_DELETED,
            COMMUNITY_NOT_QUESTION_AUTHOR,
            COMMUNITY_CONTENT_IS_REQUIRED,
            COMMUNITY_FILE_IS_EMPTY,
            VIDEO_NOT_FOUND
    ))),
    COMMUNITY_QUESTION_DELETE(new LinkedHashSet<>(Set.of(
            COMMUNITY_QUESTION_ID_IS_REQUIRED,
            COMMUNITY_QUESTION_NOT_FOUND,
            COMMUNITY_QUESTION_ALREADY_DELETED,
            COMMUNITY_NOT_QUESTION_AUTHOR
    ))),
    COMMUNITY_ANSWER_SEARCH(new LinkedHashSet<>(Set.of(
            COMMUNITY_QUESTION_ID_IS_REQUIRED,
            COMMUNITY_QUESTION_NOT_FOUND
    ))),
    COMMUNITY_ANSWER_ACCEPTED_SEARCH(new LinkedHashSet<>(Set.of(
            COMMUNITY_QUESTION_ID_IS_REQUIRED,
            COMMUNITY_QUESTION_NOT_FOUND
    ))),
    COMMUNITY_ANSWER_CREATE(new LinkedHashSet<>(Set.of(
            COMMUNITY_QUESTION_ID_IS_REQUIRED,
            COMMUNITY_QUESTION_NOT_FOUND,
            USER_NOT_FOUND,
            COMMUNITY_CONTENT_IS_REQUIRED,
            COMMUNITY_FILE_IS_EMPTY,
            COMMUNITY_MAX_FILE_COUNT_EXCEEDED,
            VIDEO_NOT_FOUND
    ))),
    COMMUNITY_ANSWER_UPDATE(new LinkedHashSet<>(Set.of(
            COMMUNITY_ANSWER_ID_IS_REQUIRED,
            COMMUNITY_ANSWER_NOT_FOUND,
            COMMUNITY_MAX_FILE_COUNT_EXCEEDED,
            COMMUNITY_CONTENT_IS_REQUIRED,
            COMMUNITY_ANSWER_ALREADY_DELETED,
            COMMUNITY_NOT_ANSWER_AUTHOR,
            COMMUNITY_FILE_IS_EMPTY,
            VIDEO_NOT_FOUND
    ))),
    COMMUNITY_ANSWER_DELETE(new LinkedHashSet<>(Set.of(
            COMMUNITY_ANSWER_ID_IS_REQUIRED,
            COMMUNITY_ANSWER_NOT_FOUND,
            COMMUNITY_ANSWER_ALREADY_DELETED,
            COMMUNITY_NOT_ANSWER_AUTHOR
    ))),
    COMMUNITY_ANSWER_ACCEPT(new LinkedHashSet<>(Set.of(
            COMMUNITY_QUESTION_ID_IS_REQUIRED,
            COMMUNITY_ANSWER_ID_IS_REQUIRED,
            COMMUNITY_QUESTION_NOT_FOUND,
            COMMUNITY_ANSWER_NOT_FOUND,
            COMMUNITY_QUESTION_ALREADY_DELETED,
            COMMUNITY_ANSWER_ALREADY_DELETED,
            COMMUNITY_NOT_ANSWER_AUTHOR,
            COMMUNITY_ANSWER_NOT_BELONG_TO_QUESTION,
            COMMUNITY_ANSWER_INVALID_USER_ID,
            COMMUNITY_ALREADY_ACCEPTED_ANSWER,
            COMMUNITY_ANSWER_ACCEPT_PERMISSION_DENIED
    ))),
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
