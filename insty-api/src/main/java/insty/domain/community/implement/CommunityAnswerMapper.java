package insty.domain.community.implement;

import insty.domain.community.dto.CommunityAnswerRes;
import insty.model.community.CommunityAnswer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommunityAnswerMapper {

    private final CommunityAnswerFileReader communityAnswerFileReader;
    private final CommunityAnswerVideoManager communityAnswerVideoManager;

    public CommunityAnswerRes toCommunityAnswerRes(CommunityAnswer answer){
        return CommunityAnswerRes.from(
                answer,
                communityAnswerFileReader.getAnswerFileInfos(answer),
                communityAnswerVideoManager.getAnswerVideoInfo(answer)
        );
    }

}
