package insty.domain.community.service;

import insty.domain.community.dto.CommunityAnswerReq;
import insty.domain.community.dto.CommunityAnswerRes;
import insty.domain.community.dto.CommunityQuestionReq;
import insty.domain.community.dto.CommunityQuestionRes;

import java.util.List;

public interface CommunityService {

    CommunityQuestionRes getQuestionDetails(String questionId);

    CommunityQuestionRes saveQuestion(CommunityQuestionReq communityQuestionReq);

    List<CommunityAnswerRes> getAllAnswers();

    CommunityAnswerRes saveAnswer(CommunityAnswerReq communityAnswerReq);

    CommunityAnswerRes updateAnswer(CommunityAnswerReq communityAnswerReq);

    CommunityAnswerRes deleteAnswer(CommunityAnswerReq communityAnswerReq);

    CommunityAnswerRes getAIAnswerRecommendation(CommunityAnswerReq communityAnswerReq);

    CommunityAnswerRes postAnswerImage(CommunityAnswerReq communityAnswerReq);

    CommunityAnswerReq postAnswerVideo(CommunityAnswerReq communityAnswerReq);
}
