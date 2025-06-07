package insty.domain.community.service;

import insty.domain.community.dto.CommunityAnswerReq;
import insty.domain.community.dto.CommunityAnswerRes;
import insty.domain.community.dto.CommunityQuestionReq;
import insty.domain.community.dto.CommunityQuestionRes;
import insty.domain.community.implement.CommunityReader;
import insty.domain.community.implement.CommunityWriter;
import insty.domain.course.implement.CourseReader;
import insty.domain.user.implement.UserReader;
import insty.model.community.CommunityAnswer;
import insty.model.community.CommunityQuestion;
import insty.model.course.Course;
import insty.model.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CommunityServiceImpl implements CommunityService {

    private final CommunityReader communityReader;
    private final CommunityWriter communityWriter;
    private final CourseReader courseReader;
    private final UserReader userReader;

    @Override
    public CommunityQuestionRes getQuestionDetails(String questionId) {
        CommunityQuestion communityQuestion = communityReader.getCommunityQuestionDetailsById(questionId);

        String title = communityQuestion.getTitle();
        String content = communityQuestion.getContent();
        Long userId = communityQuestion.getUser().getId();

        User user = userReader.getUser(userId);

        return CommunityQuestionRes.create(
                title,
                content
        );
    }

    @Override
    public List<CommunityQuestionRes> getAllQuestions() {
        List<CommunityQuestion> communityQuestions = communityReader.getAllCommunityQuestions();

        return communityQuestions.stream()
                .map(question -> CommunityQuestionRes.create(
                        question.getTitle(),
                        question.getContent()
                )).toList();
    }

    @Override
    public List<CommunityQuestionRes> getQuestionsByCourseId(String courseId) {
        List<CommunityQuestion> communityQuestions = communityReader.getAllCommunityQuestionsByCourseId(courseId);

        return communityQuestions.stream()
                .map(question -> CommunityQuestionRes.create(
                        question.getTitle(),
                        question.getContent()
                )).toList();
    }

    @Override
    public CommunityQuestionRes saveQuestion(CommunityQuestionReq communityQuestionReq) {
        Course course = courseReader.getCourseById(communityQuestionReq.courseId());
        User user = userReader.getUser(communityQuestionReq.userId());
        CommunityQuestion communityQuestion = communityWriter.saveQuestion(communityQuestionReq, course, user);

        return CommunityQuestionRes.create(
                communityQuestion.getTitle(),
                communityQuestion.getContent()
        );
    }

    @Override
    public CommunityQuestionRes updateQuestion(CommunityQuestionReq communityQuestionReq) {
        CommunityQuestion prevCommunityQuestion = communityReader.getCommunityQuestionDetailsById(String.valueOf(communityQuestionReq.questionId()));
        CommunityQuestion updatedQuestion = communityWriter.updateQuestion(prevCommunityQuestion, communityQuestionReq);

        return CommunityQuestionRes.create(
                updatedQuestion.getTitle(),
                updatedQuestion.getContent()
        );
    }

    @Override
    public void deleteQuestion(String questionId) {
        CommunityQuestion communityQuestion = communityReader.getCommunityQuestionDetailsById(String.valueOf(questionId));
        communityWriter.deleteQuestion(communityQuestion);
    }

    @Override
    public List<CommunityAnswerRes> getAllAnswers(String questionId) {
        List<CommunityAnswer> communityAnswers = communityReader.getAllCommunityAnswers(questionId);

        return communityAnswers.stream()
                .map(answer -> CommunityAnswerRes.create(
                        answer.getContent()))
                .toList();
    }

    @Override
    public CommunityAnswerRes saveAnswer(CommunityAnswerReq communityAnswerReq) {
        CommunityQuestion communityQuestion = communityReader.getCommunityQuestionDetailsById(String.valueOf(communityAnswerReq.questionId()));
        User user = userReader.getUser(communityAnswerReq.userId());
        CommunityAnswer communityAnswer = communityWriter.saveAnswer(communityQuestion, communityAnswerReq, user);

        return CommunityAnswerRes.create(
                communityAnswer.getContent()
        );

    }

    @Override
    public CommunityAnswerRes updateAnswer(CommunityAnswerReq communityAnswerReq) {
        CommunityAnswer prevCommunityAnswer = communityReader.getCommunityAnswerById(String.valueOf(communityAnswerReq.answerId()));
        CommunityAnswer updateAnswer = communityWriter.updateAnswer(prevCommunityAnswer, communityAnswerReq);

        return CommunityAnswerRes.create(
                updateAnswer.getContent()
        );
    }

    @Override
    public void deleteAnswer(CommunityAnswerReq communityAnswerReq) {
        CommunityAnswer communityAnswer = communityReader.getCommunityAnswerById(String.valueOf(communityAnswerReq.answerId()));
        communityWriter.deleteAnswer(communityAnswer);
    }

    @Override
    public CommunityAnswerRes getAIAnswerRecommendation(CommunityAnswerReq communityAnswerReq) {
        return null;
    }

    @Override
    public CommunityAnswerRes postAnswerImage(CommunityAnswerReq communityAnswerReq) {
        return null;
    }

    @Override
    public CommunityAnswerReq postAnswerVideo(CommunityAnswerReq communityAnswerReq) {
        return null;
    }
}
