package insty.domain.community.implement;

import insty.domain.community.dto.CommunityAnswerCreateReq;
import insty.domain.community.dto.CommunityAnswerUpdateReq;
import insty.domain.community.dto.CommunityQuestionCreateReq;
import insty.domain.community.dto.CommunityQuestionUpdateReq;
import insty.domain.community.repository.CommunityAnswerRepository;
import insty.domain.community.repository.CommunityQuestionRepository;
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

    private final CommunityQuestionRepository communityQuestionRepository;
    private final CommunityAnswerRepository communityAnswerRepository;

    /**
     * 질문 ID가 유효한지 검증 (존재 여부 + 삭제 여부 확인)
     */
    public void validateQuestionExists(Long questionId) {
        CommunityQuestion question = communityQuestionRepository.findById(questionId)
                .orElseThrow(() -> new CustomException(CommunityErrorCode.COMMUNITY_QUESTION_NOT_FOUND));

        if (question.isDeleted()) {
            throw new CustomException(CommunityErrorCode.COMMUNITY_QUESTION_ALREADY_DELETED);
        }
    }

    /**
     * 답변 ID가 유효한지 검증 (존재 여부 + 삭제 여부 확인)
     */
    public void validateAnswerExists(Long answerId) {
        CommunityAnswer answer = communityAnswerRepository.findById(answerId)
                .orElseThrow(() -> new CustomException(CommunityErrorCode.COMMUNITY_ANSWER_NOT_FOUND));

        if (answer.isDeleted()) {
            throw new CustomException(CommunityErrorCode.COMMUNITY_ANSWER_ALREADY_DELETED);
        }
    }

    /**
     * 질문 작성자 검증
     */
    public void validateQuestionAuthor(Long userId, Long questionId) {
        CommunityQuestion question = communityQuestionRepository.findById(questionId)
                .orElseThrow(() -> new CustomException(CommunityErrorCode.COMMUNITY_QUESTION_NOT_FOUND));
        Long authorId = question.getUser().getId();
        if (!authorId.equals(userId)) {
            throw new CustomException(CommunityErrorCode.COMMUNITY_NOT_QUESTION_AUTHOR);
        }
    }

    /**
     * 답변 작성자 검증
     */
    public void validateAnswerAuthor(Long userId, Long answerId) {
        CommunityAnswer question = communityAnswerRepository.findById(answerId)
                .orElseThrow(() -> new CustomException(CommunityErrorCode.COMMUNITY_ANSWER_NOT_FOUND));
        Long authorId = question.getUser().getId();
        if (!authorId.equals(userId)) {
            throw new CustomException(CommunityErrorCode.COMMUNITY_NOT_ANSWER_AUTHOR);
        }
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
     * 답변 내용이 비어있거나 공백만으로 이루어졌는지 검증
     */
    public void validateContent(String content) {
        if (content == null || content.trim().isEmpty()) {
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