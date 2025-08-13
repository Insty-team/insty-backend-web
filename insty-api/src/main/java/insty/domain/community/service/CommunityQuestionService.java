package insty.domain.community.service;


import insty.domain.common.FileInfo;
import insty.domain.common.SearchRes;
import insty.domain.common.dto.PaginationReq;
import insty.domain.common.dto.PaginationRes;
import insty.domain.community.dto.CommunityAnswerRes;
import insty.domain.community.dto.CommunityQuestionCreateReq;
import insty.domain.community.dto.CommunityQuestionDetailsRes;
import insty.domain.community.dto.CommunityQuestionRes;
import insty.domain.community.dto.CommunityQuestionSearchFilter;
import insty.domain.community.dto.CommunityQuestionSearchInfo;
import insty.domain.community.dto.CommunityQuestionSearchReq;
import insty.domain.community.dto.CommunityQuestionUpdateReq;
import insty.domain.community.event.CommunityQuestionCreatedEvent;
import insty.domain.community.implement.CommunityAnswerFileWriter;
import insty.domain.community.implement.CommunityAnswerVideoManager;
import insty.domain.community.implement.CommunityAnswerWriter;
import insty.domain.community.implement.CommunityQuestionFileReader;
import insty.domain.community.implement.CommunityQuestionFileWriter;
import insty.domain.community.implement.CommunityQuestionReader;
import insty.domain.community.implement.CommunityQuestionVideoManager;
import insty.domain.community.implement.CommunityQuestionWriter;
import insty.domain.community.implement.CommunityValidator;
import insty.domain.course.implement.CourseReader;
import insty.domain.user.implement.UserReader;
import insty.model.community.CommunityAnswer;
import insty.model.community.CommunityQuestion;
import insty.model.course.Course;
import insty.model.user.User;
import insty.model.video.VideoQuestion;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
public class CommunityQuestionService {
    private final CommunityQuestionReader communityQuestionReader;
    private final CommunityQuestionWriter communityQuestionWriter;
    private final CommunityQuestionFileReader communityQuestionFileReader;
    private final CommunityQuestionFileWriter communityQuestionFileWriter;
    private final CommunityQuestionVideoManager communityQuestionVideoManager;
    private final CommunityValidator communityValidator;
    private final CourseReader courseReader;
    private final UserReader userReader;
    private final CommunityAnswerService communityAnswerService;
    private final CommunityAnswerWriter communityAnswerWriter;
    private final CommunityAnswerFileWriter communityAnswerFileWriter;
    private final CommunityAnswerVideoManager communityAnswerVideoManager;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 새로운 커뮤니티 질문을 생성하고 첨부 파일을 저장
     */
    public CommunityQuestionDetailsRes saveQuestion(Long userId, CommunityQuestionCreateReq req, List<MultipartFile> attachments) {
        communityValidator.validateContent(req.content());
        communityValidator.validateFiles(attachments);

        Course course = courseReader.getCourseById(req.courseId());
        User user = userReader.getUser(userId);

        CommunityQuestion question = communityQuestionWriter.saveQuestion(user, course, req);
        List<FileInfo> fileInfos = communityQuestionFileWriter.saveQuestionFiles(question, attachments);
        VideoQuestion video = communityQuestionVideoManager.attachVideoToQuestion(question, req.videoUuid());

        eventPublisher.publishEvent(new CommunityQuestionCreatedEvent(question.getId()));

        return CommunityQuestionDetailsRes.from(question, fileInfos, video, null);
    }

    /**
     * 기존 질문을 수정하고 첨부 파일을 업데이트
     */
    public CommunityQuestionDetailsRes updateQuestion(Long userId, Long questionId, CommunityQuestionUpdateReq req, List<MultipartFile> attachments) {
        communityValidator.validateContent(req.content());
        communityValidator.validateFiles(attachments);
        communityValidator.validateQuestionAuthor(userId, questionId);

        CommunityQuestion updatedQuestion = communityQuestionWriter.updateQuestion(questionId, req);

        List<FileInfo> fileInfos = communityQuestionFileWriter.updateQuestionFiles(updatedQuestion, attachments, req.deleteFileIds());
        VideoQuestion video = communityQuestionVideoManager.updateAndGetLinkedVideo(updatedQuestion, req.videoUuid());
        List<CommunityAnswerRes> answers = communityAnswerService.getAllAnswersByQuestionId(questionId);

        return CommunityQuestionDetailsRes.from(updatedQuestion, fileInfos, video, answers);
    }

    /**
     * 커뮤니티 질문을 필터, 정렬, 키워드, 페이지네이션 조건으로 검색
     */
    public SearchRes<CommunityQuestionRes> searchQuestions(CommunityQuestionSearchReq req) {
        PaginationReq paginationReq = req.toPaginationReq();
        CommunityQuestionSearchFilter filter = req.toFilter(null, null);
        String sort = req.orderByClause();

        List<CommunityQuestionSearchInfo> questions = communityQuestionReader.searchQuestions(paginationReq, filter, sort);
        List<CommunityQuestionRes> communityQuestionRes = questions.stream()
                .map(CommunityQuestionRes::from)
                .toList();
        PaginationRes paginationRes = communityQuestionReader.countSearchQuestions(paginationReq, filter);
        return SearchRes.from(paginationRes, communityQuestionRes);
    }

    /**
     * User(러너)가 작성한 커뮤니티 질문을 검색
     */
    public SearchRes<CommunityQuestionRes> searchQuestionsByUserId(CommunityQuestionSearchReq req, Long userId){
        PaginationReq paginationReq = req.toPaginationReq();
        CommunityQuestionSearchFilter filter = req.toFilter(userId, null);
        String sort = req.orderByClause();

        List<CommunityQuestionSearchInfo> questions = communityQuestionReader.searchQuestions(paginationReq, filter, sort);
        List<CommunityQuestionRes> communityQuestionRes = questions.stream()
                .map(CommunityQuestionRes::from)
                .toList();
        PaginationRes paginationRes = communityQuestionReader.countSearchQuestions(paginationReq, filter);
        return SearchRes.from(paginationRes, communityQuestionRes);
    }

    /**
     * 특정 코스의 질문 목록 조회
     */
    public SearchRes<CommunityQuestionRes> searchQuestionsByCourseId(CommunityQuestionSearchReq req, Long courseId) {
        PaginationReq paginationReq = req.toPaginationReq();
        CommunityQuestionSearchFilter filter = req.toFilter(null, courseId);
        String sort = req.orderByClause();

        List<CommunityQuestionSearchInfo> questions = communityQuestionReader.searchQuestions(paginationReq, filter, sort);
        List<CommunityQuestionRes> communityQuestionRes = questions.stream()
                .map(CommunityQuestionRes::from)
                .toList();
        PaginationRes paginationRes = communityQuestionReader.countSearchQuestions(paginationReq, filter);
        return SearchRes.from(paginationRes, communityQuestionRes);
    }

    /**
     * 질문 상세 조회 (첨부 파일 포함)
     */
    public CommunityQuestionDetailsRes getQuestionDetails(Long questionId) {
        CommunityQuestion question = communityQuestionReader.getCommunityQuestionWithFilesById(questionId);

        List<FileInfo> fileInfos =  communityQuestionFileReader.getQuestionFileInfos(question);
        VideoQuestion video = communityQuestionVideoManager.getVideoQuestion(question);
        List<CommunityAnswerRes> answers = communityAnswerService.getAllAnswersByQuestionId(questionId);

        return CommunityQuestionDetailsRes.from(question, fileInfos, video, answers);
    }

    /**
     * 질문과 관련된 모든 데이터(답변, 첨부 파일 등)를 함께 삭제
     */
    public void deleteQuestion(Long userId, Long questionId) {
        CommunityQuestion question = communityQuestionReader.getCommunityQuestionWithAnswerById(questionId);
        communityValidator.validateQuestionAuthor(userId, questionId);
        
        // 연관된 모든 답변 삭제
        for (CommunityAnswer answer : question.getAnswers()) {
            communityAnswerFileWriter.deleteAnswerFiles(answer);
            communityAnswerVideoManager.deleteAnswerVideo(answer);
            communityAnswerWriter.deleteAnswer(answer);
        }

        communityQuestionFileWriter.deleteQuestionFiles(question);
        communityQuestionVideoManager.deleteeQuestionVideo(question);
        communityQuestionWriter.deleteQuestion(question);
    }
}