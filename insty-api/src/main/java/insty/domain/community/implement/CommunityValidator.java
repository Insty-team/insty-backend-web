package insty.domain.community.implement;

import insty.domain.community.dto.CommunityAnswerReq;
import insty.domain.community.dto.CommunityQuestionReq;
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

    public void validateQuestionOwner(Long questionId, Long userId) {
        CommunityQuestion question = communityReader.getCommunityQuestionDetailsById(questionId.toString());
        if (!question.getUser().getId().equals(userId)) {
            throw new CustomException(CommunityErrorCode.COMMUNITY_ANSWER_ACCEPT_PERMISSION_DENIED);
        }
    }

    public void validateAnswerOwner(Long answerId, Long userId) {
        CommunityAnswer answer = communityReader.getCommunityAnswerById(answerId.toString());
        if (!answer.getUser().getId().equals(userId)) {
            throw new CustomException(CommunityErrorCode.COMMUNITY_ANSWER_ACCEPT_PERMISSION_DENIED);
        }
    }

    public CommunityQuestion validateQuestionExists(Long questionId) {
        return communityReader.getCommunityQuestionDetailsById(questionId.toString());
    }

    public CommunityAnswer validateAnswerExists(Long answerId) {
        return communityReader.getCommunityAnswerById(answerId.toString());
    }

    public void validateAnswerBelongsToQuestion(CommunityAnswer answer, CommunityQuestion question) {
        if (!answer.getCommunityQuestion().getId().equals(question.getId())) {
            throw new CustomException(CommunityErrorCode.COMMUNITY_ANSWER_NOT_BELONG_TO_QUESTION);
        }
    }

    public void validateQuestionRequest(CommunityQuestionReq req) {
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

    public void validateAnswerRequest(CommunityAnswerReq req) {
        if (req.content() == null || req.content().trim().isEmpty()) {
            throw new CustomException(CommunityErrorCode.COMMUNITY_CONTENT_IS_REQUIRED);
        }
        if (req.questionId() == null || req.questionId().trim().isEmpty()) {
            throw new CustomException(CommunityErrorCode.COMMUNITY_QUESTION_ID_IS_REQUIRED);
        }
        if (req.userId() == null) {
            throw new CustomException(CommunityErrorCode.COMMUNITY_USER_ID_IS_REQUIRED);
        }
    }

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