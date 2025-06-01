package insty.domain.community.service;

import insty.domain.community.dto.CommunityQuestionRes;
import insty.domain.community.implement.CommunityReader;
import insty.model.community.CommunityQuestion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CommunityServiceImpl implements CommunityService {

    private final CommunityReader communityReader;

    @Override
    public CommunityQuestionRes getQuestionDetails(String questionId) {
        CommunityQuestion communityQuestion = communityReader.getQuestionDetailsById(questionId);

        String title = communityQuestion.getTitle();
        String content = communityQuestion.getContent();
        
        //ToDo : user table 추가 후 user_id 정보 포함

        return CommunityQuestionRes.create(
                title,
                content
        );
    }

    @Override
    public CommunityQuestionRes saveAnswer(CommunityQuestionRes communityQuestionRes) {
        return null;
    }
}
