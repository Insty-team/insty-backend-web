package insty.domain.community.implement;

import insty.domain.community.dto.CommunityAnswerCreateReq;
import insty.domain.community.dto.CommunityAnswerUpdateReq;
import insty.domain.community.dto.CommunityQuestionCreateReq;
import insty.domain.community.dto.CommunityQuestionUpdateReq;
import insty.error.CommunityErrorCode;
import insty.exception.CustomException;
import insty.model.community.CommunityAnswer;
import insty.model.community.CommunityQuestion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommunityValidator {

    private final CommunityReader communityReader;

    /**
     * 질문 작성자 검증
     */
    public void validateQuestionOwner(Long questionId, Long userId) {
        CommunityQuestion question = communityReader.getCommunityQuestionDetailsById(questionId);
        if (!question.getUser().getId().equals(userId)) {
            throw new CustomException(CommunityErrorCode.COMMUNITY_ANSWER_ACCEPT_PERMISSION_DENIED);
        }
    }

    /**
     * 답변 작성자 검증
     */
    public void validateAnswerOwner(Long answerId, Long userId) {
        CommunityAnswer answer = communityReader.getCommunityAnswerById(answerId);
        if (!answer.getUser().getId().equals(userId)) {
            throw new CustomException(CommunityErrorCode.COMMUNITY_ANSWER_ACCEPT_PERMISSION_DENIED);
        }
    }

    /**
     * 질문 존재 여부 검증
     */
    public CommunityQuestion validateQuestionExists(Long questionId) {
        return communityReader.getCommunityQuestionDetailsById(questionId);
    }

    /**
     * 답변 존재 여부 검증
     */
    public CommunityAnswer validateAnswerExists(Long answerId) {
        return communityReader.getCommunityAnswerById(answerId);
    }

    /**
     * 답변이 해당 질문에 속하는지 검증
     */
    public void validateAnswerBelongsToQuestion(CommunityAnswer answer, CommunityQuestion question) {
        if (!answer.getCommunityQuestion().getId().equals(question.getId())) {
            throw new CustomException(CommunityErrorCode.COMMUNITY_ANSWER_NOT_BELONG_TO_QUESTION);
        }
    }

    /**
     * 질문 생성 요청 데이터 검증
     */
    public void validateQuestionCreateRequest(CommunityQuestionCreateReq req) {
        if (req.title() == null || req.title().trim().isEmpty()) {
            throw new CustomException(CommunityErrorCode.COMMUNITY_TITLE_IS_REQUIRED);
        }
        if (req.content() == null || req.content().trim().isEmpty()) {
            throw new CustomException(CommunityErrorCode.COMMUNITY_CONTENT_IS_REQUIRED);
        }
        if (req.courseId() == null) {
            throw new CustomException(CommunityErrorCode.COMMUNITY_COURSE_ID_IS_REQUIRED);
        }
        if (req.userId() == null) {
            throw new CustomException(CommunityErrorCode.COMMUNITY_USER_ID_IS_REQUIRED);
        }
    }

    /**
     * 질문 수정 요청 데이터 검증
     */
    public void validateQuestionUpdateRequest(CommunityQuestionUpdateReq req) {
        // todo : id 검증
        if (req.title() == null || req.title().trim().isEmpty()) {
            throw new CustomException(CommunityErrorCode.COMMUNITY_TITLE_IS_REQUIRED);
        }
        if (req.content() == null || req.content().trim().isEmpty()) {
            throw new CustomException(CommunityErrorCode.COMMUNITY_CONTENT_IS_REQUIRED);
        }
    }

    /**
     * 답변 생성 요청 데이터 검증
     */
    public void validateAnswerCreateRequest(CommunityAnswerCreateReq req) {
        if (req.content() == null || req.content().trim().isEmpty()) {
            throw new CustomException(CommunityErrorCode.COMMUNITY_CONTENT_IS_REQUIRED);
        }
        if (req.userId() == null) {
            throw new CustomException(CommunityErrorCode.COMMUNITY_USER_ID_IS_REQUIRED);
        }
    }

    /**
     * 답변 수정 요청 데이터 검증
     */
    public void validateAnswerUpdateRequest(CommunityAnswerUpdateReq req) {
        // todo : 수정하고자하는 답변 ID 누락
        if (req.answerId() == null) {
            throw new CustomException(CommunityErrorCode.COMMUNITY_ANSWER_NOT_BELONG_TO_QUESTION);
        }
        if (req.content() == null || req.content().trim().isEmpty()) {
            throw new CustomException(CommunityErrorCode.COMMUNITY_CONTENT_IS_REQUIRED);
        }

    }

    /**
     * 파일 검증
     */
    public void validateFiles(List<MultipartFile> files) {
        if (files == null) {
            return;
        }

        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                throw new CustomException(CommunityErrorCode.COMMUNITY_FILE_IS_EMPTY);
            }
            // todo : 파일 크기, 확장자 등 추가 검증 로직
        }
    }

    /**
     * 비디오 UUID 검증 및 파싱
     */
    public UUID validateAndParseVideoUuid(String videoUuid) {
        if (videoUuid == null || videoUuid.trim().isEmpty()) {
            return null;
        }

        try {
            return UUID.fromString(videoUuid);
        } catch (IllegalArgumentException e) {
            throw new CustomException(CommunityErrorCode.COMMUNITY_INVALID_VIDEO_UUID);
        }
    }
}