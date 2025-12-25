package insty.domain.courseqna.service;


import insty.domain.common.FileInfo;
import insty.domain.common.SearchRes;
import insty.domain.common.dto.PaginationReq;
import insty.domain.common.dto.PaginationRes;
import insty.domain.courseqna.dto.CommunityQuestionCreateReq;
import insty.domain.courseqna.dto.CommunityQuestionDetailsRes;
import insty.domain.courseqna.dto.CommunityQuestionMyRes;
import insty.domain.courseqna.dto.CommunityQuestionRes;
import insty.domain.courseqna.dto.CourseQuestionSearchFilter;
import insty.domain.courseqna.dto.CourseQuestionSearchInfo;
import insty.domain.courseqna.dto.CommunityQuestionSearchReq;
import insty.domain.courseqna.dto.CommunityQuestionUpdateReq;
import insty.domain.courseqna.implement.CommunityAnswerFileWriter;
import insty.domain.courseqna.implement.CommunityAnswerVideoManager;
import insty.domain.courseqna.implement.CommunityAnswerWriter;
import insty.domain.courseqna.implement.CommunityNotificationManager;
import insty.domain.courseqna.implement.CommunityQuestionFileReader;
import insty.domain.courseqna.implement.CommunityQuestionFileWriter;
import insty.domain.courseqna.implement.CommunityQuestionReader;
import insty.domain.courseqna.implement.CommunityQuestionVideoManager;
import insty.domain.courseqna.implement.CommunityQuestionViewManager;
import insty.domain.courseqna.implement.CommunityQuestionWriter;
import insty.domain.courseqna.implement.CommunityValidator;
import insty.domain.course.implement.CourseReader;
import insty.domain.user.implement.UserReader;
import insty.model.courseqna.CourseAnswer;
import insty.model.courseqna.CourseQuestion;
import insty.model.course.Course;
import insty.model.user.User;
import insty.model.video.VideoQuestion;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
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
    private final CommunityAnswerWriter communityAnswerWriter;
    private final CommunityAnswerFileWriter communityAnswerFileWriter;
    private final CommunityAnswerVideoManager communityAnswerVideoManager;
    private final CommunityNotificationManager communityNotificationManager;
    private final CommunityQuestionViewManager communityQuestionViewManager;

    /**
     * 새로운 커뮤니티 질문을 생성하고 첨부 파일을 저장
     */
    public CommunityQuestionDetailsRes saveQuestion(Long userId, CommunityQuestionCreateReq req, List<MultipartFile> attachments) {
        communityValidator.validateContent(req.content());
        communityValidator.validateFiles(attachments);

        // 기획 상, 최대 파일은 2개로 제한
        communityValidator.validateQuestionFileCount(attachments);

        Course course = courseReader.getCourseById(req.courseId());
        User user = userReader.getUser(userId);

        CourseQuestion question = communityQuestionWriter.saveQuestion(user, course, req);
        List<FileInfo> fileInfos = communityQuestionFileWriter.saveQuestionFiles(question, attachments);
        VideoQuestion video = communityQuestionVideoManager.attachVideoToQuestion(question, req.videoUuid());

        communityQuestionViewManager.recordQuestionView(question, userId);

        communityNotificationManager.sendNewQuestionNotification(question);

        return CommunityQuestionDetailsRes.from(question, fileInfos, video);
    }

    /**
     * 기존 질문을 수정하고 첨부 파일을 업데이트
     */
    public CommunityQuestionDetailsRes updateQuestion(Long userId, Long questionId, CommunityQuestionUpdateReq req, List<MultipartFile> attachments) {
        communityValidator.validateContent(req.content());
        communityValidator.validateFiles(attachments);
        communityValidator.validateQuestionAuthor(userId, questionId);

        // 질문 가져와서 파일 개수 검증
        CourseQuestion question = communityQuestionReader.getCommunityQuestionWithFilesById(questionId);
        communityValidator.validateQuestionFileCountForUpdate(question, attachments, req.deleteFileIds());

        CourseQuestion updatedQuestion = communityQuestionWriter.updateQuestion(questionId, req);

        List<FileInfo> fileInfos = communityQuestionFileWriter.updateQuestionFiles(updatedQuestion, attachments, req.deleteFileIds());
        VideoQuestion video = communityQuestionVideoManager.updateAndGetLinkedVideo(updatedQuestion, req.videoUuid());

        return CommunityQuestionDetailsRes.from(updatedQuestion, fileInfos, video);
    }

    /**
     * 커뮤니티 질문을 필터, 정렬, 키워드, 페이지네이션 조건으로 검색
     */
    public SearchRes<CommunityQuestionRes> searchQuestions(CommunityQuestionSearchReq req) {
        PaginationReq paginationReq = req.toPaginationReq();
        CourseQuestionSearchFilter filter = req.toFilter(null, null);
        String sort = req.orderByClause();

        List<CourseQuestionSearchInfo> questions = communityQuestionReader.searchQuestions(paginationReq, filter,
                sort);
        List<CommunityQuestionRes> communityQuestionRes = questions.stream()
                .map(CommunityQuestionRes::from)
                .toList();
        PaginationRes paginationRes = communityQuestionReader.countSearchQuestions(paginationReq, filter);
        return SearchRes.from(paginationRes, communityQuestionRes);
    }

    /**
     * User(러너)가 작성한 커뮤니티 질문을 검색
     */
    public SearchRes<CommunityQuestionMyRes> searchQuestionsByUserId(CommunityQuestionSearchReq req, Long userId) {
        PaginationReq paginationReq = req.toPaginationReq();
        CourseQuestionSearchFilter filter = req.toFilter(userId, null);
        String sort = req.orderByClause();

        List<CourseQuestionSearchInfo> questions = communityQuestionReader.searchQuestions(paginationReq, filter, sort);
        
        List<Long> questionIds = questions.stream()
                .map(CourseQuestionSearchInfo::id)
                .toList();

        Map<Long, Long> answerCounts = communityQuestionReader.getAnswerCountsByQuestionIds(questionIds);
        Map<Long, Boolean> hasNewAnswers = communityQuestionViewManager.getHasNewAnswersForQuestions(questionIds, userId);

        List<CommunityQuestionMyRes> items = questions.stream()
                .map(info -> CommunityQuestionMyRes.from(
                        info,
                        answerCounts.getOrDefault(info.id(), 0L),
                        hasNewAnswers.getOrDefault(info.id(), false)
                ))
                .toList();

        PaginationRes paginationRes = communityQuestionReader.countSearchQuestions(paginationReq, filter);
        return SearchRes.from(paginationRes, items);
    }

    /**
     * 특정 코스의 질문 목록 조회
     */
    public SearchRes<CommunityQuestionRes> searchQuestionsByCourseId(CommunityQuestionSearchReq req, Long courseId) {
        PaginationReq paginationReq = req.toPaginationReq();
        CourseQuestionSearchFilter filter = req.toFilter(null, courseId);
        String sort = req.orderByClause();

        List<CourseQuestionSearchInfo> questions = communityQuestionReader.searchQuestions(paginationReq, filter, sort);
        List<CommunityQuestionRes> communityQuestionRes = questions.stream()
                .map(CommunityQuestionRes::from)
                .toList();
        PaginationRes paginationRes = communityQuestionReader.countSearchQuestions(paginationReq, filter);
        return SearchRes.from(paginationRes, communityQuestionRes);
    }

    /**
     * 질문 상세 조회 (첨부 파일 포함)
     */
    public CommunityQuestionDetailsRes getQuestionDetails(Long questionId, Long userId) {
        CourseQuestion question = communityQuestionReader.getCommunityQuestionWithFilesById(questionId);

        List<FileInfo> fileInfos =  communityQuestionFileReader.getQuestionFileInfos(question);
        VideoQuestion video = communityQuestionVideoManager.getVideoQuestion(question);

        CommunityQuestionDetailsRes response = CommunityQuestionDetailsRes.from(question, fileInfos, video);
        
        communityQuestionViewManager.recordQuestionViewIfAuthorOrCreator(questionId, userId);
        
        return response;
    }

    /**
     * 질문과 관련된 모든 데이터(답변, 첨부 파일 등)를 함께 삭제
     */
    public void deleteQuestion(Long userId, Long questionId) {
        CourseQuestion question = communityQuestionReader.getCommunityQuestionWithAnswerById(questionId);
        communityValidator.validateQuestionAuthor(userId, questionId);
        
        // 연관된 모든 답변 삭제
        for (CourseAnswer answer : question.getAnswers()) {
            communityAnswerFileWriter.deleteAnswerFiles(answer);
            communityAnswerVideoManager.deleteAnswerVideo(answer);
            communityAnswerWriter.deleteAnswer(answer);
        }

        communityQuestionFileWriter.deleteQuestionFiles(question);
        communityQuestionVideoManager.deleteQuestionVideo(question);
        communityQuestionWriter.deleteQuestion(question);
    }
}