package insty.domain.courseqna.service;


import insty.domain.common.FileInfo;
import insty.domain.common.SearchRes;
import insty.domain.common.dto.PaginationReq;
import insty.domain.common.dto.PaginationRes;
import insty.domain.courseqna.dto.CourseQuestionCreateReq;
import insty.domain.courseqna.dto.CourseQuestionDetailsRes;
import insty.domain.courseqna.dto.CourseQuestionMyRes;
import insty.domain.courseqna.dto.CourseQuestionRes;
import insty.domain.courseqna.dto.CourseQuestionSearchFilter;
import insty.domain.courseqna.dto.CourseQuestionSearchInfo;
import insty.domain.courseqna.dto.CourseQuestionSearchReq;
import insty.domain.courseqna.dto.CourseQuestionUpdateReq;
import insty.domain.courseqna.implement.CourseAnswerFileWriter;
import insty.domain.courseqna.implement.CourseAnswerVideoManager;
import insty.domain.courseqna.implement.CourseAnswerWriter;
import insty.domain.courseqna.implement.CourseNotificationManager;
import insty.domain.courseqna.implement.CourseQuestionFileReader;
import insty.domain.courseqna.implement.CourseQuestionFileWriter;
import insty.domain.courseqna.implement.CourseQuestionReader;
import insty.domain.courseqna.implement.CourseQuestionVideoManager;
import insty.domain.courseqna.implement.CourseQuestionViewManager;
import insty.domain.courseqna.implement.CourseQuestionWriter;
import insty.domain.courseqna.implement.CourseQnaValidator;
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
public class CourseQuestionService {
    private final CourseQuestionReader courseQuestionReader;
    private final CourseQuestionWriter courseQuestionWriter;
    private final CourseQuestionFileReader courseQuestionFileReader;
    private final CourseQuestionFileWriter courseQuestionFileWriter;
    private final CourseQuestionVideoManager courseQuestionVideoManager;
    private final CourseQnaValidator courseQnaValidator;
    private final CourseReader courseReader;
    private final UserReader userReader;
    private final CourseAnswerWriter courseAnswerWriter;
    private final CourseAnswerFileWriter courseAnswerFileWriter;
    private final CourseAnswerVideoManager courseAnswerVideoManager;
    private final CourseNotificationManager courseNotificationManager;
    private final CourseQuestionViewManager courseQuestionViewManager;

    /**
     * 새로운 강좌 질문을 생성하고 첨부 파일을 저장
     */
    public CourseQuestionDetailsRes saveQuestion(Long userId, Long courseId, CourseQuestionCreateReq req, List<MultipartFile> attachments) {
        courseQnaValidator.validateContent(req.content());
        courseQnaValidator.validateFiles(attachments);

        // 기획 상, 최대 파일은 2개로 제한
        courseQnaValidator.validateQuestionFileCount(attachments);

        Course course = courseReader.getCourseById(courseId);
        User user = userReader.getUser(userId);

        CourseQuestion question = courseQuestionWriter.saveQuestion(user, course, req);
        List<FileInfo> fileInfos = courseQuestionFileWriter.saveQuestionFiles(question, attachments);
        VideoQuestion video = courseQuestionVideoManager.attachVideoToQuestion(question, req.videoUuid());

        courseQuestionViewManager.recordQuestionView(question, userId);

        courseNotificationManager.sendNewQuestionNotification(question);

        return CourseQuestionDetailsRes.from(question, fileInfos, video);
    }

    /**
     * 기존 질문을 수정하고 첨부 파일을 업데이트
     */
    public CourseQuestionDetailsRes updateQuestion(Long userId, Long questionId, CourseQuestionUpdateReq req, List<MultipartFile> attachments) {
        courseQnaValidator.validateContent(req.content());
        courseQnaValidator.validateFiles(attachments);
        courseQnaValidator.validateQuestionAuthor(userId, questionId);

        // 질문 가져와서 파일 개수 검증
        CourseQuestion question = courseQuestionReader.getCourseQuestionWithFilesById(questionId);
        courseQnaValidator.validateQuestionFileCountForUpdate(question, attachments, req.deleteFileIds());

        CourseQuestion updatedQuestion = courseQuestionWriter.updateQuestion(questionId, req);

        List<FileInfo> fileInfos = courseQuestionFileWriter.updateQuestionFiles(updatedQuestion, attachments, req.deleteFileIds());
        VideoQuestion video = courseQuestionVideoManager.updateAndGetLinkedVideo(updatedQuestion, req.videoUuid());

        return CourseQuestionDetailsRes.from(updatedQuestion, fileInfos, video);
    }

    /**
     * 강좌 질문을 필터, 정렬, 키워드, 페이지네이션 조건으로 검색
     */
    public SearchRes<CourseQuestionRes> searchQuestions(CourseQuestionSearchReq req) {
        PaginationReq paginationReq = req.toPaginationReq();
        CourseQuestionSearchFilter filter = req.toFilter(null, null);
        String sort = req.orderByClause();

        List<CourseQuestionSearchInfo> questions = courseQuestionReader.searchQuestions(paginationReq, filter,
                sort);
        List<CourseQuestionRes> courseQuestionRes = questions.stream()
                .map(CourseQuestionRes::from)
                .toList();
        PaginationRes paginationRes = courseQuestionReader.countSearchQuestions(paginationReq, filter);
        return SearchRes.from(paginationRes, courseQuestionRes);
    }

    /**
     * User(러너)가 작성한 강좌 질문을 검색
     */
    public SearchRes<CourseQuestionMyRes> searchQuestionsByUserId(CourseQuestionSearchReq req, Long userId) {
        PaginationReq paginationReq = req.toPaginationReq();
        CourseQuestionSearchFilter filter = req.toFilter(userId, null);
        String sort = req.orderByClause();

        List<CourseQuestionSearchInfo> questions = courseQuestionReader.searchQuestions(paginationReq, filter, sort);
        
        List<Long> questionIds = questions.stream()
                .map(CourseQuestionSearchInfo::id)
                .toList();

        Map<Long, Long> answerCounts = courseQuestionReader.getAnswerCountsByQuestionIds(questionIds);
        Map<Long, Boolean> hasNewAnswers = courseQuestionViewManager.getHasNewAnswersForQuestions(questionIds, userId);

        List<CourseQuestionMyRes> items = questions.stream()
                .map(info -> CourseQuestionMyRes.from(
                        info,
                        answerCounts.getOrDefault(info.id(), 0L),
                        hasNewAnswers.getOrDefault(info.id(), false)
                ))
                .toList();

        PaginationRes paginationRes = courseQuestionReader.countSearchQuestions(paginationReq, filter);
        return SearchRes.from(paginationRes, items);
    }

    /**
     * 특정 코스의 질문 목록 조회
     */
    public SearchRes<CourseQuestionRes> searchQuestionsByCourseId(CourseQuestionSearchReq req, Long courseId) {
        PaginationReq paginationReq = req.toPaginationReq();
        CourseQuestionSearchFilter filter = req.toFilter(null, courseId);
        String sort = req.orderByClause();

        List<CourseQuestionSearchInfo> questions = courseQuestionReader.searchQuestions(paginationReq, filter, sort);
        List<CourseQuestionRes> courseQuestionRes = questions.stream()
                .map(CourseQuestionRes::from)
                .toList();
        PaginationRes paginationRes = courseQuestionReader.countSearchQuestions(paginationReq, filter);
        return SearchRes.from(paginationRes, courseQuestionRes);
    }

    /**
     * 질문 상세 조회 (첨부 파일 포함)
     */
    public CourseQuestionDetailsRes getQuestionDetails(Long questionId, Long userId) {
        CourseQuestion question = courseQuestionReader.getCourseQuestionWithFilesById(questionId);

        List<FileInfo> fileInfos =  courseQuestionFileReader.getQuestionFileInfos(question);
        VideoQuestion video = courseQuestionVideoManager.getVideoQuestion(question);

        CourseQuestionDetailsRes response = CourseQuestionDetailsRes.from(question, fileInfos, video);
        
        courseQuestionViewManager.recordQuestionViewIfAuthorOrCreator(questionId, userId);
        
        return response;
    }

    /**
     * 질문과 관련된 모든 데이터(답변, 첨부 파일 등)를 함께 삭제
     */
    public void deleteQuestion(Long userId, Long questionId) {
        CourseQuestion question = courseQuestionReader.getCourseQuestionWithAnswerById(questionId);
        courseQnaValidator.validateQuestionAuthor(userId, questionId);
        
        // 연관된 모든 답변 삭제
        for (CourseAnswer answer : question.getAnswers()) {
            courseAnswerFileWriter.deleteAnswerFiles(answer);
            courseAnswerVideoManager.deleteAnswerVideo(answer);
            courseAnswerWriter.deleteAnswer(answer);
        }

        courseQuestionFileWriter.deleteQuestionFiles(question);
        courseQuestionVideoManager.deleteQuestionVideo(question);
        courseQuestionWriter.deleteQuestion(question);
    }
}
