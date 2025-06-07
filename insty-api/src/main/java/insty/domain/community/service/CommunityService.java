package insty.domain.community.service;

import insty.domain.community.dto.CommunityAnswerReq;
import insty.domain.community.dto.CommunityAnswerRes;
import insty.domain.community.dto.CommunityQuestionReq;
import insty.domain.community.dto.CommunityQuestionRes;

import java.util.List;

public interface CommunityService {

    CommunityQuestionRes getQuestionDetails(String questionId);

    List<CommunityQuestionRes> getQuestionsByCourseId(String courseId);

    List<CommunityQuestionRes> getAllQuestions();

    CommunityQuestionRes saveQuestion(CommunityQuestionReq communityQuestionReq);

    CommunityQuestionRes updateQuestion(CommunityQuestionReq communityQuestionReq);

    void deleteQuestion(String questionId);

    List<CommunityAnswerRes> getAllAnswers(String questionId);

    CommunityAnswerRes saveAnswer(CommunityAnswerReq communityAnswerReq);

    CommunityAnswerRes updateAnswer(CommunityAnswerReq communityAnswerReq);

    void deleteAnswer(CommunityAnswerReq communityAnswerReq);

    CommunityAnswerRes getAIAnswerRecommendation(CommunityAnswerReq communityAnswerReq);

    CommunityAnswerRes postAnswerImage(CommunityAnswerReq communityAnswerReq);

    CommunityAnswerReq postAnswerVideo(CommunityAnswerReq communityAnswerReq);
}
