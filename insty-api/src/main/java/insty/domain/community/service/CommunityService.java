package insty.domain.community.service;

import insty.domain.common.FileInfo;
import insty.domain.common.SearchRes;
import insty.domain.common.dto.PaginationReq;
import insty.domain.common.dto.PaginationRes;
import insty.domain.community.dto.*;
import insty.domain.community.implement.CommunityAnswerAcceptService;
import insty.domain.community.implement.CommunityComplexReader;
import insty.domain.community.implement.CommunityFileManager;
import insty.domain.community.implement.CommunityReader;
import insty.domain.community.implement.CommunityValidator;
import insty.domain.community.implement.CommunityWriter;
import insty.domain.community.implement.CommunityQuestionReader;
import insty.domain.community.implement.CommunityQuestionWriter;
import insty.domain.community.implement.CommunityQuestionFileWriter;
import insty.domain.community.implement.CommunityAnswerReader;
import insty.domain.community.implement.CommunityAnswerWriter;
import insty.domain.community.implement.CommunityAnswerFileWriter;
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
    private final CommunityQuestionReader communityQuestionReader;
    private final CommunityQuestionWriter communityQuestionWriter;
    private final CommunityComplexReader communityComplexReader;
    private final CommunityQuestionFileWriter communityQuestionFileWriter;
    private final CommunityAnswerReader communityAnswerReader;
    private final CommunityAnswerWriter communityAnswerWriter;
    private final CommunityAnswerFileWriter communityAnswerFileWriter;
    private final CommunityValidator communityValidator;
    private final CommunityAnswerAcceptService communityAnswerAcceptService;
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
    public List<CommunityQuestionRes> getQuestionsByCourseId(Long courseId) {
        return communityQuestionReader.getAllCommunityQuestionsByCourseId(courseId).stream()
                .map(q -> getQuestionDetails(q.getId()))
                .toList();
    }

    /**
     * 질문 상세 조회 (첨부 파일 포함)
     */
    public CommunityQuestionRes getQuestionDetails(Long questionId) {
        // 질문 조회
        CommunityQuestion question = communityQuestionReader.getCommunityQuestionDetailsById(questionId);

        // 답변 목록 생성 (각 답변의 첨부 파일 포함)
        List<CommunityAnswerRes> answers = question.getAnswers().stream()
                .map(answer -> {
                    List<CommunityAnswerFile> answerFiles = communityAnswerReader.getCommunityAnswerFilesByAnswerId(answer.getId());
                    List<FileInfo> fileInfos = answerFiles == null ? List.of() : answerFiles.stream().map(f -> FileInfo.from(f.getFile(), "")).toList();
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
        List<FileInfo> questionAttachments = question.getAttachments() == null ? List.of() : question.getAttachments().stream().map(f -> FileInfo.from(f.getFile(), "")).toList();

        // 채택된 답변 처리 (있는 경우에만)
        CommunityAnswerRes acceptedAnswerRes = null;
        if (question.getAcceptedAnswer() != null) {
            CommunityAnswer acceptedAnswer = question.getAcceptedAnswer();
            List<CommunityAnswerFile> acceptedAnswerFiles = communityAnswerReader.getCommunityAnswerFilesByAnswerId(acceptedAnswer.getId());
            List<FileInfo> acceptedAnswerFileInfos = acceptedAnswerFiles == null ? List.of() : acceptedAnswerFiles.stream().map(f -> FileInfo.from(f.getFile(), "")).toList();
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
    public CommunityQuestionRes saveQuestion(CommunityQuestionCreateReq req, List<MultipartFile> attachments) {
        // 요청 데이터 검증
        communityValidator.validateQuestionCreateRequest(req);
        communityValidator.validateFiles(attachments);

        // 관련 엔티티 조회
        Course course = courseReader.getCourseById(req.courseId());
        User user = userReader.getUser(req.userId());

        // 질문 엔티티 생성 및 저장
        CommunityQuestion question = communityQuestionWriter.saveQuestion(user, course, req);

        // 첨부 파일 저장 및 FileInfo 변환
        List<FileInfo> fileInfos = communityQuestionFileWriter.saveQuestionFiles(question, attachments);

        // 비디오 등록
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
    public CommunityQuestionRes updateQuestion(Long questionId, CommunityQuestionUpdateReq req, List<MultipartFile> attachments) {
        // 요청 데이터 검증
        communityValidator.validateQuestionUpdateRequest(req);
        communityValidator.validateFiles(attachments);

        // 기존 질문 조회
        CommunityQuestion prevQuestion = communityQuestionReader.getCommunityQuestionDetailsById(questionId);
        List<CommunityFile> existingAttachments = prevQuestion.getAttachments();

        // 질문 업데이트
        CommunityQuestion updatedQuestion = communityQuestionWriter.updateQuestion(questionId, req);

        // 첨부 파일 처리
        List<FileInfo> updatedFileInfos;
        if (attachments != null && !attachments.isEmpty()) {
            // 새 파일이 첨부된 경우: 기존 파일 삭제 후 새 파일 저장
            communityQuestionFileWriter.deleteQuestionFiles(existingAttachments);
            updatedFileInfos = communityQuestionFileWriter.saveQuestionFiles(updatedQuestion, attachments);
        } else {
            // 새 파일이 없는 경우: 기존 파일 유지
            updatedFileInfos = existingAttachments == null ? List.of() : existingAttachments.stream().map(f -> FileInfo.from(f.getFile(), "")).toList();
        }

        List<CommunityAnswerRes> answers = updatedQuestion.getAnswers().stream()
                .map(answer -> {
                    List<CommunityAnswerFile> answerFiles = communityAnswerReader.getCommunityAnswerFilesByAnswerId(answer.getId());
                    List<FileInfo> fileInfos = answerFiles == null ? List.of() : answerFiles.stream().map(f -> FileInfo.from(f.getFile(), "")).toList();
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
            List<CommunityAnswerFile> acceptedAnswerFiles = communityAnswerReader.getCommunityAnswerFilesByAnswerId(acceptedAnswer.getId());
            List<FileInfo> acceptedAnswerFileInfos = acceptedAnswerFiles == null ? List.of() : acceptedAnswerFiles.stream().map(f -> FileInfo.from(f.getFile(), "")).toList();
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
    public void deleteQuestion(Long questionId) {
        CommunityQuestion question = communityQuestionReader.getCommunityQuestionDetailsById(questionId);
        communityQuestionWriter.deleteQuestion(question);
    }

    /// ============================== 답변 API  ======================================

    /**
     * 특정 질문에 달린 모든 답변을 상세 정보와 함께 조회
     */
    public List<CommunityAnswerRes> getAllAnswers(Long questionId) {
        return communityAnswerReader.getAllCommunityAnswers(questionId).stream()
                .map(answer -> getAnswerDetails(answer.getId()))
                .toList();
    }

    /**
     * 답변의 모든 정보와 첨부 파일을 포함하여 조회
     */
    public CommunityAnswerRes getAnswerDetails(Long answerId) {
        // 답변 조회
        CommunityAnswer answer = communityAnswerReader.getCommunityAnswerById(answerId);

        // 답변 첨부 파일 조회 및 변환
        List<CommunityAnswerFile> answerFiles = communityAnswerReader.getCommunityAnswerFilesByAnswerId(answerId);
        List<FileInfo> fileInfos = answerFiles == null ? List.of() : answerFiles.stream().map(f -> FileInfo.from(f.getFile(), "")).toList();

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
    public CommunityAnswerRes saveAnswer(CommunityAnswerCreateReq req, List<MultipartFile> imageFiles, String videoUuid) {
        // 요청 데이터 검증
        communityValidator.validateAnswerCreateRequest(req);
        communityValidator.validateFiles(imageFiles);

        // 관련 엔티티 조회
        CommunityQuestion question = communityQuestionReader.getCommunityQuestionDetailsById(req.questionId());
        User user = userReader.getUser(req.userId());

        // 답변 저장
        CommunityAnswer answer = communityAnswerWriter.saveAnswer(user, question, req);

        // 이미지 파일 저장
        communityAnswerFileWriter.saveAnswerImageFiles(answer, imageFiles);

        // 비디오 파일 처리 (UUID 검증 및 저장)
        UUID videoUuidObj = communityValidator.validateAndParseVideoUuid(videoUuid);
        if (videoUuidObj != null) {
            // TODO: 영상 파일 저장 로직은 기존대로 유지
        }

        // 저장된 파일 정보 조회 및 응답 생성
        List<CommunityAnswerFile> answerFiles = communityAnswerReader.getCommunityAnswerFilesByAnswerId(answer.getId());
        List<FileInfo> fileInfos = answerFiles == null ? List.of() : answerFiles.stream().map(f -> FileInfo.from(f.getFile(), "")).toList();

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
    public CommunityAnswerRes updateAnswer(Long answerId, CommunityAnswerUpdateReq req, List<MultipartFile> imageFiles, String videoUuid) {
        // 요청 데이터 검증
        communityValidator.validateAnswerUpdateRequest(req);
        communityValidator.validateFiles(imageFiles);

        // 기존 답변 조회 및 수정
        CommunityAnswer prevAnswer = communityAnswerReader.getCommunityAnswerById(answerId);
        CommunityAnswer updatedAnswer = communityAnswerWriter.updateAnswer(answerId, req);

        // 기존 파일 조회
        List<CommunityAnswerFile> existingAnswerFiles = communityAnswerReader.getCommunityAnswerFilesByAnswerId(answerId);

        // 이미지 파일 처리
        if (imageFiles != null && !imageFiles.isEmpty()) {
            // 새 이미지가 첨부된 경우: 기존 파일 삭제 후 새 파일 저장
            communityAnswerFileWriter.deleteAnswerFiles(existingAnswerFiles);
            communityAnswerFileWriter.saveAnswerImageFiles(updatedAnswer, imageFiles);
        }

        // 비디오 파일 처리
        UUID videoUuidObj = communityValidator.validateAndParseVideoUuid(videoUuid);
        if (videoUuidObj != null) {
            // 새 비디오가 첨부된 경우: 기존 파일 삭제 후 새 파일 저장
            communityAnswerFileWriter.deleteAnswerFiles(existingAnswerFiles);
            // TODO: 영상 파일 저장 로직은 기존대로 유지
        }

        // 6. 업데이트된 파일 정보 조회 및 응답 생성
        List<CommunityAnswerFile> updatedAnswerFiles = communityAnswerReader.getCommunityAnswerFilesByAnswerId(answerId);
        List<FileInfo> fileInfos = updatedAnswerFiles == null ? List.of() : updatedAnswerFiles.stream().map(f -> FileInfo.from(f.getFile(), "")).toList();

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
    public void deleteAnswer(Long answerId) {
        CommunityAnswer answer = communityAnswerReader.getCommunityAnswerById(answerId);
        communityAnswerWriter.deleteAnswer(answer);
    }


    /// ============================== 답변 채택 API  ======================================

    /**
     * 질문 작성자가 특정 답변을 채택
     * -> 한 질문에는 하나의 답변만 채택할 수 있습니다.
     */
    public void acceptAnswer(Long questionId, Long answerId) {
        // 1. 질문과 답변 조회
        CommunityQuestion question = communityQuestionReader.getCommunityQuestionDetailsById(questionId);
        CommunityAnswer answer = communityAnswerReader.getCommunityAnswerById(answerId);

        // 2. 답변이 해당 질문에 속하는지 검증
        communityValidator.validateAnswerBelongsToQuestion(answer, question);

        // 3. 답변 채택 처리
        communityAnswerAcceptService.acceptAnswer(question, answer);
    }
}
