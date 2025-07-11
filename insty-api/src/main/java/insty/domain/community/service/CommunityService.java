package insty.domain.community.service;

import insty.domain.common.FileInfo;
import insty.domain.community.dto.*;
import insty.domain.community.implement.CommunityFileManager;
import insty.domain.community.implement.CommunityReader;
import insty.domain.community.implement.CommunityValidator;
import insty.domain.community.implement.CommunityWriter;
import insty.domain.course.implement.CourseReader;
import insty.domain.user.implement.UserReader;
import insty.model.community.CommunityAnswer;
import insty.model.community.CommunityAnswerFile;
import insty.model.community.CommunityFile;
import insty.model.community.CommunityQuestion;
import insty.model.course.Course;
import insty.model.user.User;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
public class CommunityService {
    private final CommunityReader communityReader;
    private final CommunityWriter communityWriter;
    private final CommunityValidator communityValidator;
    private final CommunityFileManager communityFileManager;
    private final CourseReader courseReader;
    private final UserReader userReader;

    public CommunityQuestionRes saveQuestion(CommunityQuestionReq req, List<MultipartFile> attachments) {
        communityValidator.validateQuestionRequest(req);
        communityValidator.validateFiles(attachments);
        Course course = courseReader.getCourseById(req.courseId());
        User user = userReader.getUser(req.userId());
        CommunityQuestion question = CommunityQuestion.create(course, user, req.title(), req.content());
        question = communityWriter.saveQuestion(question, course, user);
        List<FileInfo> fileInfos = communityFileManager.saveQuestionFiles(question, attachments);
        return CommunityQuestionRes.create(
                user.getId(),
                course.getId(),
                question.getTitle(),
                question.getContent(),
                question.getCreatedAt(),
                question.getUpdatedAt(),
                List.of(),
                fileInfos,
                null
        );
    }

    public CommunityQuestionRes updateQuestion(CommunityQuestionReq req, List<MultipartFile> attachments) {
        communityValidator.validateQuestionRequest(req);
        communityValidator.validateFiles(attachments);
        CommunityQuestion prevQuestion = communityReader.getCommunityQuestionDetailsById(String.valueOf(req.questionId()));
        List<CommunityFile> existingAttachments = prevQuestion.getAttachments();
        CommunityQuestion updatedQuestion = communityWriter.updateQuestion(prevQuestion, req, attachments);
        List<FileInfo> updatedFileInfos;
        if (attachments != null && !attachments.isEmpty()) {
            communityFileManager.deleteQuestionFiles(existingAttachments);
            updatedFileInfos = communityFileManager.saveQuestionFiles(updatedQuestion, attachments);
        } else {
            updatedFileInfos = communityFileManager.convertToFileInfos(existingAttachments);
        }
        List<CommunityAnswerRes> answers = updatedQuestion.getAnswers().stream()
                .map(answer -> {
                    List<CommunityAnswerFile> answerFiles = communityReader.getCommunityAnswerFilesByAnswerId(answer.getId().toString());
                    List<FileInfo> fileInfos = communityFileManager.convertAnswerFilesToFileInfos(answerFiles);
                    return CommunityAnswerRes.create(
                            answer.getUser().getId(),
                            answer.getContent(),
                            fileInfos,
                            answer.getCreatedAt(),
                            answer.getUpdatedAt(),
                            answer.isAccepted()
                    );
                })
                .toList();
        CommunityAnswerRes acceptedAnswerRes = null;
        if (updatedQuestion.getAcceptedAnswer() != null) {
            CommunityAnswer acceptedAnswer = updatedQuestion.getAcceptedAnswer();
            List<CommunityAnswerFile> acceptedAnswerFiles = communityReader.getCommunityAnswerFilesByAnswerId(acceptedAnswer.getId().toString());
            List<FileInfo> acceptedAnswerFileInfos = communityFileManager.convertAnswerFilesToFileInfos(acceptedAnswerFiles);
            acceptedAnswerRes = CommunityAnswerRes.create(
                    acceptedAnswer.getUser().getId(),
                    acceptedAnswer.getContent(),
                    acceptedAnswerFileInfos,
                    acceptedAnswer.getCreatedAt(),
                    acceptedAnswer.getUpdatedAt(),
                    acceptedAnswer.isAccepted()
            );
        }
        return CommunityQuestionRes.create(
                updatedQuestion.getUser().getId(),
                updatedQuestion.getCourse().getId(),
                updatedQuestion.getTitle(),
                updatedQuestion.getContent(),
                updatedQuestion.getCreatedAt(),
                updatedQuestion.getUpdatedAt(),
                answers,
                updatedFileInfos,
                acceptedAnswerRes
        );
    }

    public void deleteQuestion(String questionId) {
        CommunityQuestion question = communityReader.getCommunityQuestionDetailsById(questionId);
        communityWriter.deleteQuestion(question);
    }

    public CommunityQuestionRes getQuestionDetails(String questionId) {
        CommunityQuestion question = communityReader.getCommunityQuestionDetailsById(questionId);
        List<CommunityAnswerRes> answers = question.getAnswers().stream()
                .map(answer -> {
                    List<CommunityAnswerFile> answerFiles = communityReader.getCommunityAnswerFilesByAnswerId(answer.getId().toString());
                    List<FileInfo> fileInfos = communityFileManager.convertAnswerFilesToFileInfos(answerFiles);
                    return CommunityAnswerRes.create(
                            answer.getUser().getId(),
                            answer.getContent(),
                            fileInfos,
                            answer.getCreatedAt(),
                            answer.getUpdatedAt(),
                            answer.isAccepted()
                    );
                })
                .toList();
        List<FileInfo> questionAttachments = communityFileManager.convertToFileInfos(question.getAttachments());
        CommunityAnswerRes acceptedAnswerRes = null;
        if (question.getAcceptedAnswer() != null) {
            CommunityAnswer acceptedAnswer = question.getAcceptedAnswer();
            List<CommunityAnswerFile> acceptedAnswerFiles = communityReader.getCommunityAnswerFilesByAnswerId(acceptedAnswer.getId().toString());
            List<FileInfo> acceptedAnswerFileInfos = communityFileManager.convertAnswerFilesToFileInfos(acceptedAnswerFiles);
            acceptedAnswerRes = CommunityAnswerRes.create(
                    acceptedAnswer.getUser().getId(),
                    acceptedAnswer.getContent(),
                    acceptedAnswerFileInfos,
                    acceptedAnswer.getCreatedAt(),
                    acceptedAnswer.getUpdatedAt(),
                    acceptedAnswer.isAccepted()
            );
        }
        return CommunityQuestionRes.create(
                question.getUser().getId(),
                question.getCourse().getId(),
                question.getTitle(),
                question.getContent(),
                question.getCreatedAt(),
                question.getUpdatedAt(),
                answers,
                questionAttachments,
                acceptedAnswerRes
        );
    }

    public List<CommunityQuestionRes> getAllQuestions() {
        return communityReader.getAllCommunityQuestions().stream()
                .map(q -> getQuestionDetails(q.getId().toString()))
                .toList();
    }

    public List<CommunityQuestionRes> getQuestionsByCourseId(String courseId) {
        return communityReader.getAllCommunityQuestionsByCourseId(courseId).stream()
                .map(q -> getQuestionDetails(q.getId().toString()))
                .toList();
    }

    public CommunityAnswerRes saveAnswer(CommunityAnswerReq req, List<MultipartFile> imageFiles, String videoUuid) {
        communityValidator.validateAnswerRequest(req);
        communityValidator.validateFiles(imageFiles);
        CommunityQuestion question = communityReader.getCommunityQuestionDetailsById(req.questionId());
        User user = userReader.getUser(req.userId());
        CommunityAnswer answer = communityWriter.saveAnswer(question, req, user);
        communityFileManager.saveAnswerImageFiles(answer, imageFiles);
        UUID videoUuidObj = communityValidator.validateAndParseVideoUuid(videoUuid);
        if (videoUuidObj != null) {
            // todo: 영상 파일 저장 로직은 기존대로 유지
        }
        List<CommunityAnswerFile> answerFiles = communityReader.getCommunityAnswerFilesByAnswerId(answer.getId().toString());
        List<FileInfo> fileInfos = communityFileManager.convertAnswerFilesToFileInfos(answerFiles);
        return CommunityAnswerRes.create(
                user.getId(),
                answer.getContent(),
                fileInfos,
                answer.getCreatedAt(),
                answer.getUpdatedAt(),
                answer.isAccepted()
        );
    }

    public CommunityAnswerRes updateAnswer(CommunityAnswerReq req, List<MultipartFile> imageFiles, String videoUuid) {
        communityValidator.validateAnswerRequest(req);
        communityValidator.validateFiles(imageFiles);
        CommunityAnswer prevAnswer = communityReader.getCommunityAnswerById(req.answerId());
        CommunityAnswer updatedAnswer = communityWriter.updateAnswer(prevAnswer, req);
        List<CommunityAnswerFile> existingAnswerFiles = communityReader.getCommunityAnswerFilesByAnswerId(req.answerId());
        if (imageFiles != null && !imageFiles.isEmpty()) {
            communityFileManager.deleteAnswerFiles(existingAnswerFiles);
            communityFileManager.saveAnswerImageFiles(updatedAnswer, imageFiles);
        }
        UUID videoUuidObj = communityValidator.validateAndParseVideoUuid(videoUuid);
        if (videoUuidObj != null) {
            communityFileManager.deleteAnswerFiles(existingAnswerFiles);
            // todo: 영상 파일 저장 로직은 기존대로 유지
        }
        List<CommunityAnswerFile> updatedAnswerFiles = communityReader.getCommunityAnswerFilesByAnswerId(req.answerId());
        List<FileInfo> fileInfos = communityFileManager.convertAnswerFilesToFileInfos(updatedAnswerFiles);
        return CommunityAnswerRes.create(
                updatedAnswer.getUser().getId(),
                updatedAnswer.getContent(),
                fileInfos,
                updatedAnswer.getCreatedAt(),
                updatedAnswer.getUpdatedAt(),
                updatedAnswer.isAccepted()
        );
    }

    public void deleteAnswer(String answerId) {
        CommunityAnswer answer = communityReader.getCommunityAnswerById(answerId);
        communityWriter.deleteAnswer(answer);
    }

    public CommunityAnswerRes getAnswerDetails(String answerId) {
        CommunityAnswer answer = communityReader.getCommunityAnswerById(answerId);
        List<CommunityAnswerFile> answerFiles = communityReader.getCommunityAnswerFilesByAnswerId(answerId);
        List<FileInfo> fileInfos = communityFileManager.convertAnswerFilesToFileInfos(answerFiles);
        return CommunityAnswerRes.create(
                answer.getUser().getId(),
                answer.getContent(),
                fileInfos,
                answer.getCreatedAt(),
                answer.getUpdatedAt(),
                answer.isAccepted()
        );
    }

    public List<CommunityAnswerRes> getAllAnswers(String questionId) {
        return communityReader.getAllCommunityAnswers(questionId).stream()
                .map(answer -> getAnswerDetails(answer.getId().toString()))
                .toList();
    }

    public void acceptAnswer(String questionId, String answerId) {
        CommunityQuestion question = communityReader.getCommunityQuestionDetailsById(questionId);
        CommunityAnswer answer = communityReader.getCommunityAnswerById(answerId);
        communityValidator.validateAnswerBelongsToQuestion(answer, question);
        communityWriter.acceptAnswer(question, answer);
    }

    public void unacceptAnswer(String questionId) {
        CommunityQuestion question = communityReader.getCommunityQuestionDetailsById(questionId);
        communityWriter.unacceptAnswer(question);
    }
}
