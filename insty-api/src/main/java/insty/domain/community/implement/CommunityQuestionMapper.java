package insty.domain.community.implement;

import insty.domain.community.dto.CommunityQuestionDetailsRes;
import insty.domain.community.dto.CommunityQuestionRes;
import insty.domain.community.dto.CommunityQuestionUpdateReq;
import insty.domain.community.dto.CommunityUserRes;
import insty.model.community.CommunityQuestion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommunityQuestionMapper {

    private final CommunityQuestionFileReader communityQuestionFileReader;
    private final CommunityQuestionVideoManager communityQuestionVideoManager;
    private final CommunityAnswerMapper answerMapper;

    public CommunityQuestionRes toCommunityQuestionRes(CommunityQuestion question){
        return CommunityQuestionRes.from(question);
    }

    public CommunityQuestionDetailsRes toCommunityQuestionDetailsRes(CommunityQuestion question){
        return CommunityQuestionDetailsRes.from(
                question,
                communityQuestionFileReader.getQuestionFileInfos(question),
                communityQuestionVideoManager.getQuestionVideoInfos(question),
                question.getAnswers().stream().map(answerMapper::toCommunityAnswerRes).toList()
        );
    }
}
