package insty.domain.community.service;


import insty.domain.common.FileInfo;
import insty.domain.common.SearchRes;
import insty.domain.common.VideoInfo;
import insty.domain.common.dto.PaginationReq;
import insty.domain.common.dto.PaginationRes;
import insty.domain.community.dto.*;
import insty.domain.community.implement.*;
import insty.domain.course.implement.CourseReader;
import insty.domain.user.implement.UserReader;
import insty.model.community.CommunityQuestion;
import insty.model.course.Course;
import insty.model.user.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import insty.domain.community.dto.CommunityAnswerRes;

@Service
@Transactional
@RequiredArgsConstructor
public class CommunityQuestionService {
    private final CommunityQuestionReader communityQuestionReader;
    private final CommunityQuestionWriter communityQuestionWriter;
    private final CommunityQuestionFileReader communityQuestionFileReader;
    private final CommunityQuestionFileWriter communityQuestionFileWriter;
    private final CommunityQuestionVideoManager communityQuestionVideoManager;
    private final CommunityAnswerMapper communityAnswerMapper;
    private final CommunityQuestionMapper communityQuestionMapper;
    private final CommunityValidator communityValidator;
    private final CourseReader courseReader;
    private final UserReader userReader;

    /**
     * 커뮤니티 질문을 필터, 정렬, 키워드, 페이지네이션 조건으로 검색
     */
    public SearchRes<CommunityQuestionRes> searchQuestions(CommunityQuestionSearchReq req) {
        PaginationReq paginationReq = req.toPaginationReq();
        CommunityQuestionSearchFilter filter = req.toSearchFilter();
        String sort = req.sort();

        List<CommunityQuestion> questions = communityQuestionReader.searchQuestions(paginationReq, filter, sort);
        List<CommunityQuestionRes> communityQuestionRes = questions.stream()
                .map(communityQuestionMapper::toCommunityQuestionRes)
                .toList();

        PaginationRes paginationRes = communityQuestionReader.countSearchQuestions(paginationReq, filter);
        return SearchRes.from(paginationRes, communityQuestionRes);
    }

    /**
     * User(러너)가 작성한 커뮤니티 질문을 검색
     */
    public SearchRes<CommunityQuestionRes> searchQuestionsByUserId(CommunityQuestionSearchReq req, Long userId){
        PaginationReq paginationReq = req.toPaginationReq();
        CommunityQuestionSearchFilter filter = req.toSearchFilterWithUser(userId);
        String sort = req.sort();

        List<CommunityQuestion> questions = communityQuestionReader.searchQuestions(paginationReq, filter, sort);
        List<CommunityQuestionRes> communityQuestionRes = questions.stream()
                .map(communityQuestionMapper::toCommunityQuestionRes)
                .toList();

        PaginationRes paginationRes = communityQuestionReader.countSearchQuestions(paginationReq, filter);
        return SearchRes.from(paginationRes, communityQuestionRes);
    }

    /**
     * 특정 코스의 질문 목록 조회
     */
    public List<CommunityQuestionRes> getQuestionsByCourseId(Long courseId) {
        List<CommunityQuestion> questions = communityQuestionReader.getAllCommunityQuestionsByCourseId(courseId);
        List<CommunityQuestionRes> communityQuestionRes = questions.stream()
                .map(communityQuestionMapper::toCommunityQuestionRes)
                .toList();

        return communityQuestionRes;
    }

    /**
     * 질문 상세 조회 (첨부 파일 포함)
     */
    public CommunityQuestionDetailsRes getQuestionDetails(Long questionId) {
        CommunityQuestion question = communityQuestionReader.getCommunityQuestionDetailsById(questionId);

        List<FileInfo> questionFileInfos =  communityQuestionFileReader.getQuestionFileInfos(question);
        List<VideoInfo> videoInfos = communityQuestionVideoManager.getAnswerVideoInfos(question);
        List<CommunityAnswerRes> answers = question.getAnswers().stream()
                .map(communityAnswerMapper::toCommunityAnswerRes)
                .toList();

        return CommunityQuestionDetailsRes.from(question, questionFileInfos, videoInfos, answers);
    }

    /**
     * 새로운 커뮤니티 질문을 생성하고 첨부 파일을 저장
     */
    public CommunityQuestionDetailsRes saveQuestion(Long userId, CommunityQuestionCreateReq req, List<MultipartFile> attachments) {
        communityValidator.validateQuestionCreateRequest(req);
        communityValidator.validateFiles(attachments);

        Course course = courseReader.getCourseById(req.courseId());
        User user = userReader.getUser(userId);

        CommunityQuestion question = communityQuestionWriter.saveQuestion(user, course, req);
        List<FileInfo> fileInfos = communityQuestionFileWriter.saveQuestionFiles(question, attachments);
        List<VideoInfo> videoInfos = communityQuestionVideoManager.saveQuestionVideo(question, req.videoUuids());

        return CommunityQuestionDetailsRes.from(question, fileInfos, videoInfos, null);
    }

    /**
     * 기존 질문을 수정하고 첨부 파일을 업데이트
     */
    public CommunityQuestionDetailsRes updateQuestion(Long userId, Long questionId, CommunityQuestionUpdateReq req, List<MultipartFile> attachments) {
        communityValidator.validateQuestionUpdateRequest(req);
        communityValidator.validateFiles(attachments);

        CommunityQuestion updatedQuestion = communityQuestionWriter.updateQuestion(questionId, req);
        List<FileInfo> updatedFileInfos = communityQuestionFileWriter.updateQuestionFiles(updatedQuestion, attachments, req.deleteFileIds());
        List<VideoInfo> videoInfos = null; // todo : 비디오 업데이트 로직
        List<CommunityAnswerRes> answers = updatedQuestion.getAnswers().stream()
                .map(communityAnswerMapper::toCommunityAnswerRes)
                .toList();

        return CommunityQuestionDetailsRes.from(updatedQuestion, updatedFileInfos, videoInfos, answers);
    }

    /**
     * 질문과 관련된 모든 데이터(답변, 첨부 파일 등)를 함께 삭제
     */
    public void deleteQuestion(Long userId, Long questionId) {
        CommunityQuestion question = communityQuestionReader.getCommunityQuestionDetailsById(questionId);
        communityQuestionWriter.deleteQuestion(question);
    }
}