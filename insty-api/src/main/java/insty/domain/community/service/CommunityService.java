package insty.domain.community.service;

import insty.domain.common.FileInfo;
import insty.domain.common.SearchRes;
import insty.domain.common.dto.PaginationReq;
import insty.domain.common.dto.PaginationRes;
import insty.domain.community.dto.*;
import insty.domain.community.implement.CommunityComplexReader;
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
    private final CommunityComplexReader communityComplexReader;
    private final CommunityWriter communityWriter;
    private final CommunityValidator communityValidator;
    private final CommunityFileManager communityFileManager;
    private final CourseReader courseReader;
    private final UserReader userReader;


    /// ============================== 질문 API  ======================================

    /**
     * 커뮤니티 질문을 필터, 정렬, 키워드, 페이지네이션 조건으로 검색
     */
    public SearchRes<CommunityQuestionRes> searchQuestions(CommunityQuestionSearchReq req) {
        PaginationReq paginationReq = req.toPaginationReq();
        CommunityQuestionSearchFilter filter = req.toSearchFilter();
        String sort = req.sort();

        List<CommunityQuestionRes> questionResList = communityComplexReader.searchQuestions(paginationReq, filter, sort);
        PaginationRes paginationRes = communityComplexReader.countSearchQuestions(paginationReq, filter);
        return SearchRes.from(paginationRes, questionResList);
    }

    /**
     * 특정 코스의 질문 목록 조회
     */
    public List<CommunityQuestionRes> getQuestionsByCourseId(String courseId) {
        return communityReader.getAllCommunityQuestionsByCourseId(courseId).stream()
                .map(q -> getQuestionDetails(q.getId().toString()))
                .toList();
    }

    /**
     * 질문 상세 조회 (첨부 파일 포함)
     */
    public CommunityQuestionRes getQuestionDetails(String questionId) {
        // 질문 조회
        CommunityQuestion question = communityReader.getCommunityQuestionDetailsById(questionId);

        // 답변 목록 생성 (각 답변의 첨부 파일 포함)
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

        // 질문 첨부 파일 처리
        List<FileInfo> questionAttachments = communityFileManager.convertToFileInfos(question.getAttachments());

        // 채택된 답변 처리 (있는 경우에만)
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


    /**
     * 새로운 커뮤니티 질문을 생성하고 첨부 파일을 저장
     */
    public CommunityQuestionRes saveQuestion(CommunityQuestionReq req, List<MultipartFile> attachments) {
        // 요청 데이터 검증
        communityValidator.validateQuestionRequest(req);
        communityValidator.validateFiles(attachments);

        // 관련 엔티티 조회
        Course course = courseReader.getCourseById(req.courseId());
        User user = userReader.getUser(req.userId());

        // 질문 엔티티 생성 및 저장
        CommunityQuestion question = CommunityQuestion.create(course, user, req.title(), req.content());
        question = communityWriter.saveQuestion(question, course, user);

        // 첨부 파일 저장 및 FileInfo 변환
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

    /**
     * 기존 질문을 수정하고 첨부 파일을 업데이트
     */
    public CommunityQuestionRes updateQuestion(CommunityQuestionReq req, List<MultipartFile> attachments) {
        // 요청 데이터 검증
        communityValidator.validateQuestionRequest(req);
        communityValidator.validateFiles(attachments);

        // 기존 질문 조회
        CommunityQuestion prevQuestion = communityReader.getCommunityQuestionDetailsById(String.valueOf(req.questionId()));
        List<CommunityFile> existingAttachments = prevQuestion.getAttachments();

        // 질문 업데이트
        CommunityQuestion updatedQuestion = communityWriter.updateQuestion(prevQuestion, req, attachments);

        // 첨부 파일 처리
        List<FileInfo> updatedFileInfos;
        if (attachments != null && !attachments.isEmpty()) {
            // 새 파일이 첨부된 경우: 기존 파일 삭제 후 새 파일 저장
            communityFileManager.deleteQuestionFiles(existingAttachments);
            updatedFileInfos = communityFileManager.saveQuestionFiles(updatedQuestion, attachments);
        } else {
            // 새 파일이 없는 경우: 기존 파일 유지
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

        // 채택된 답변 처리 (있는 경우에만)
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

        // 최종 응답 데이터 생성
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

    /**
     * 질문과 관련된 모든 데이터(답변, 첨부 파일 등)를 함께 삭제
     */
    public void deleteQuestion(String questionId) {
        CommunityQuestion question = communityReader.getCommunityQuestionDetailsById(questionId);
        communityWriter.deleteQuestion(question);
    }

    /// ============================== 답변 API  ======================================

    /**
     * 특정 질문에 달린 모든 답변을 상세 정보와 함께 조회
     */
    public List<CommunityAnswerRes> getAllAnswers(String questionId) {
        return communityReader.getAllCommunityAnswers(questionId).stream()
                .map(answer -> getAnswerDetails(answer.getId().toString()))
                .toList();
    }

    /**
     * 답변의 모든 정보와 첨부 파일을 포함하여 조회
     */
    public CommunityAnswerRes getAnswerDetails(String answerId) {
        // 답변 조회
        CommunityAnswer answer = communityReader.getCommunityAnswerById(answerId);

        // 답변 첨부 파일 조회 및 변환
        List<CommunityAnswerFile> answerFiles = communityReader.getCommunityAnswerFilesByAnswerId(answerId);
        List<FileInfo> fileInfos = communityFileManager.convertAnswerFilesToFileInfos(answerFiles);

        // 응답 데이터 생성
        return CommunityAnswerRes.create(
                answer.getUser().getId(),
                answer.getContent(),
                fileInfos,
                answer.getCreatedAt(),
                answer.getUpdatedAt(),
                answer.isAccepted()
        );
    }


    /**
     * 새로운 답변을 생성하고 이미지 파일과 비디오 파일을 저장
     */
    public CommunityAnswerRes saveAnswer(CommunityAnswerReq req, List<MultipartFile> imageFiles, String videoUuid) {
        // 요청 데이터 검증
        communityValidator.validateAnswerRequest(req);
        communityValidator.validateFiles(imageFiles);

        // 관련 엔티티 조회
        CommunityQuestion question = communityReader.getCommunityQuestionDetailsById(req.questionId());
        User user = userReader.getUser(req.userId());

        // 답변 저장
        CommunityAnswer answer = communityWriter.saveAnswer(question, req, user);

        // 이미지 파일 저장
        communityFileManager.saveAnswerImageFiles(answer, imageFiles);

        // 비디오 파일 처리 (UUID 검증 및 저장)
        UUID videoUuidObj = communityValidator.validateAndParseVideoUuid(videoUuid);
        if (videoUuidObj != null) {
            // TODO: 영상 파일 저장 로직은 기존대로 유지
        }

        // 저장된 파일 정보 조회 및 응답 생성
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

    /**
     * 기존 답변을 수정하고 첨부 파일을 업데이트
     * 새로운 파일이 첨부되면 기존 파일을 삭제하고 새 파일을 저장
     */
    public CommunityAnswerRes updateAnswer(CommunityAnswerReq req, List<MultipartFile> imageFiles, String videoUuid) {
        // 요청 데이터 검증
        communityValidator.validateAnswerRequest(req);
        communityValidator.validateFiles(imageFiles);

        // 기존 답변 조회 및 수정
        CommunityAnswer prevAnswer = communityReader.getCommunityAnswerById(req.answerId());
        CommunityAnswer updatedAnswer = communityWriter.updateAnswer(prevAnswer, req);

        // 기존 파일 조회
        List<CommunityAnswerFile> existingAnswerFiles = communityReader.getCommunityAnswerFilesByAnswerId(req.answerId());

        // 이미지 파일 처리
        if (imageFiles != null && !imageFiles.isEmpty()) {
            // 새 이미지가 첨부된 경우: 기존 파일 삭제 후 새 파일 저장
            communityFileManager.deleteAnswerFiles(existingAnswerFiles);
            communityFileManager.saveAnswerImageFiles(updatedAnswer, imageFiles);
        }

        // 비디오 파일 처리
        UUID videoUuidObj = communityValidator.validateAndParseVideoUuid(videoUuid);
        if (videoUuidObj != null) {
            // 새 비디오가 첨부된 경우: 기존 파일 삭제 후 새 파일 저장
            communityFileManager.deleteAnswerFiles(existingAnswerFiles);
            // TODO: 영상 파일 저장 로직은 기존대로 유지
        }

        // 6. 업데이트된 파일 정보 조회 및 응답 생성
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

    /**
     * 답변과 관련된 모든 데이터(첨부 파일 등)를 함께 삭제
     */
    public void deleteAnswer(String answerId) {
        CommunityAnswer answer = communityReader.getCommunityAnswerById(answerId);
        communityWriter.deleteAnswer(answer);
    }


    /// ============================== 답변 채택 API  ======================================

    /**
     * 질문 작성자가 특정 답변을 채택
     * -> 한 질문에는 하나의 답변만 채택할 수 있습니다.
     */
    public void acceptAnswer(String questionId, String answerId) {
        // 1. 질문과 답변 조회
        CommunityQuestion question = communityReader.getCommunityQuestionDetailsById(questionId);
        CommunityAnswer answer = communityReader.getCommunityAnswerById(answerId);

        // 2. 답변이 해당 질문에 속하는지 검증
        communityValidator.validateAnswerBelongsToQuestion(answer, question);

        // 3. 답변 채택 처리
        communityWriter.acceptAnswer(question, answer);
    }

    /**
     * 질문 작성자가 채택된 답변을 취소
     */
    public void unacceptAnswer(String questionId) {
        CommunityQuestion question = communityReader.getCommunityQuestionDetailsById(questionId);
        communityWriter.unacceptAnswer(question);
    }
}
