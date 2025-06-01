package insty.domain.community.service;

import insty.domain.community.dto.CommunityQuestionReq;

public interface CommunityService {

    CommunityQuestionReq getQuestionDetails(String questionId);
}
