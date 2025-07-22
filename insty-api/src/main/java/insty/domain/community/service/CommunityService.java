package insty.domain.community.service;

import insty.domain.common.FileInfo;
import insty.domain.common.SearchRes;
import insty.domain.common.VideoInfo;
import insty.domain.common.dto.PaginationReq;
import insty.domain.common.dto.PaginationRes;
import insty.domain.community.dto.*;
import insty.domain.community.implement.CommunityAnswerAcceptService;
import insty.domain.community.implement.CommunityAnswerFileReader;
import insty.domain.community.implement.CommunityAnswerMapper;
import insty.domain.community.implement.CommunityAnswerVideoManager;
import insty.domain.community.implement.CommunityComplexReader;
import insty.domain.community.implement.CommunityQuestionFileReader;
import insty.domain.community.implement.CommunityQuestionMapper;
import insty.domain.community.implement.CommunityQuestionVideoManager;
import insty.domain.community.implement.CommunityValidator;
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
    private final CommunityQuestionFileReader communityQuestionFileReader;
    private final CommunityQuestionFileWriter communityQuestionFileWriter;
    private final CommunityQuestionVideoManager communityQuestionVideoManager;
    private final CommunityQuestionMapper communityQuestionMapper;
    private final CommunityComplexReader communityComplexReader;
    private final CommunityAnswerReader communityAnswerReader;
    private final CommunityAnswerWriter communityAnswerWriter;
    private final CommunityAnswerFileReader communityAnswerFileReader;
    private final CommunityAnswerFileWriter communityAnswerFileWriter;
    private final CommunityAnswerMapper communityAnswerMapper;
    private final CommunityAnswerVideoManager communityAnswerVideoManager;
    private final CommunityAnswerAcceptService communityAnswerAcceptService;
    private final CommunityValidator communityValidator;
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

        List<CommunityQuestion> questions = communityComplexReader.searchQuestions(paginationReq, filter, sort);
        List<CommunityQuestionRes> communityQuestionRes = questions.stream()
                .map(communityQuestionMapper::toCommunityQuestionRes)
                .toList();

        PaginationRes paginationRes = communityComplexReader.countSearchQuestions(paginationReq, filter);
        return SearchRes.from(paginationRes, communityQuestionRes);
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
        List<FileInfo> questionFileInfos =  communityQuestionFileReader.getQuestionFileInfos(question);
        List<VideoInfo> videoInfos = communityQuestionVideoManager.getAnswerVideoInfos(question);
        List<CommunityAnswerRes> answers = question.getAnswers().stream()
                .map(communityAnswerMapper::toCommunityAnswerRes)
                .toList();
        return CommunityQuestionRes.from(question, questionFileInfos, videoInfos, answers);
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
        List<FileInfo> fileInfos = communityQuestionFileWriter.saveQuestionFiles(question, attachments);
        List<VideoInfo> videoInfos = communityQuestionVideoManager.saveQuestionVideo(question, req.videoUuids());

        return CommunityQuestionRes.from(question, fileInfos, videoInfos, null);
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

        // todo : 비디오 업데이트 로직
        List<VideoInfo> videoInfos = null;

        List<CommunityAnswerRes> answers = updatedQuestion.getAnswers().stream()
                .map(communityAnswerMapper::toCommunityAnswerRes)
                .toList();

        return CommunityQuestionRes.from(updatedQuestion, updatedFileInfos, videoInfos, answers);
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
        List<FileInfo> fileInfos = communityAnswerFileReader.getAnswerFileInfos(answer);
        VideoInfo videoInfo = communityAnswerVideoManager.getAnswerVideoInfo(answer);

        return CommunityAnswerRes.from(answer, fileInfos, videoInfo);
    }


    /**
     * 새로운 답변을 생성하고 이미지 파일과 비디오 파일을 저장
     */
    public CommunityAnswerRes saveAnswer(CommunityAnswerCreateReq req, List<MultipartFile> imageFiles) {
        // 요청 데이터 검증
        communityValidator.validateAnswerCreateRequest(req);
        communityValidator.validateFiles(imageFiles);

        // 관련 엔티티 조회
        CommunityQuestion question = communityQuestionReader.getCommunityQuestionDetailsById(req.questionId());
        User user = userReader.getUser(req.userId());

        // 답변 저장
        CommunityAnswer answer = communityAnswerWriter.saveAnswer(user, question, req);
        List<FileInfo> fileInfos = communityAnswerFileReader.getAnswerFileInfos(answer);

        // 이미지 파일 저장
        communityAnswerFileWriter.saveAnswerImageFiles(answer, imageFiles);

        // 비디오 파일 처리 (UUID 검증 및 저장)
        // todo : 비디오 유효성 검사?
        VideoInfo videoInfo = communityAnswerVideoManager.saveAnswerVideo(answer, req.videoUuid());

        return CommunityAnswerRes.from(answer, fileInfos, videoInfo);
    }

    /**
     * 기존 답변을 수정하고 첨부 파일을 업데이트
     * 새로운 파일이 첨부되면 기존 파일을 삭제하고 새 파일을 저장
     */
    public CommunityAnswerRes updateAnswer(Long answerId, CommunityAnswerUpdateReq req, List<MultipartFile> imageFiles) {
        // 요청 데이터 검증
        communityValidator.validateAnswerUpdateRequest(req);
        communityValidator.validateFiles(imageFiles);

        // 기존 답변 조회 및 수정
        CommunityAnswer prevAnswer = communityAnswerReader.getCommunityAnswerById(answerId);
        CommunityAnswer updatedAnswer = communityAnswerWriter.updateAnswer(answerId, req);

        // 기존 파일 조회
        List<CommunityAnswerFile> existingAnswerFiles = communityAnswerReader.getCommunityAnswerFilesByAnswerId(answerId);

        // 이미지 파일 처리
        // todo : 해당 로직 수정
        if (imageFiles != null && !imageFiles.isEmpty()) {
            // 새 이미지가 첨부된 경우: 기존 파일 삭제 후 새 파일 저장
            communityAnswerFileWriter.deleteAnswerFiles(existingAnswerFiles);
            communityAnswerFileWriter.saveAnswerImageFiles(updatedAnswer, imageFiles);
        }

        // 비디오 파일 처리
        List<FileInfo> fileInfos = communityAnswerFileReader.getAnswerFileInfos(updatedAnswer);
        VideoInfo videoInfo = communityAnswerVideoManager.saveAnswerVideo(updatedAnswer, req.videoUuid());

        return CommunityAnswerRes.from(updatedAnswer, fileInfos, videoInfo);
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
    public AcceptAnswerResultRes acceptAnswer(Long questionId, Long answerId) {
        CommunityQuestion question = communityQuestionReader.getCommunityQuestionDetailsById(questionId);
        CommunityAnswer answer = communityAnswerReader.getCommunityAnswerById(answerId);

        communityValidator.validateAnswerBelongsToQuestion(answer, question);

        return communityAnswerAcceptService.acceptAnswer(question, answer);
    }
}
