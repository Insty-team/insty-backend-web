package insty.domain.community.implement;

import insty.domain.community.dto.CommunityQuestionRes;
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
        return CommunityQuestionRes.from(
                question,
                communityQuestionFileReader.getQuestionFileInfos(question),
                communityQuestionVideoManager.getAnswerVideoInfos(question),
                question.getAnswers().stream().map(answerMapper::toCommunityAnswerRes).toList()
        );
    }
}
