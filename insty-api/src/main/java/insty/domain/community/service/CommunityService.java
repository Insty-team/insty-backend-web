package insty.domain.community.service;

import insty.domain.community.dto.CommunityAnswerReq;
import insty.domain.community.dto.CommunityQuestionRes;

public interface CommunityService {

    CommunityQuestionRes getQuestionDetails(String questionId);

    void saveAnswer(CommunityAnswerReq communityAnswerReq);

}
