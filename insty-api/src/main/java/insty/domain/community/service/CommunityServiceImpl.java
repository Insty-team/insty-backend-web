package insty.domain.community.service;

import insty.domain.community.dto.CommunityAnswerReq;
import insty.domain.community.dto.CommunityAnswerRes;
import insty.domain.community.dto.CommunityQuestionReq;
import insty.domain.community.dto.CommunityQuestionRes;
import insty.domain.community.implement.CommunityReader;
import insty.domain.community.implement.CommunityWriter;
import insty.model.community.CommunityAnswer;
import insty.model.community.CommunityQuestion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CommunityServiceImpl implements CommunityService {

    private final CommunityReader communityReader;
    private final CommunityWriter communityWriter;

    @Override
    public CommunityQuestionRes getQuestionDetails(String questionId) {
        CommunityQuestion communityQuestion = communityReader.getCommunityQuestionDetailsById(questionId);

        String title = communityQuestion.getTitle();
        String content = communityQuestion.getContent();
        
        //ToDo : user table 추가 후 user_id 정보 포함

        return CommunityQuestionRes.create(
                title,
                content
        );
    }

    @Override
    public CommunityQuestionRes saveQuestion(CommunityQuestionReq communityQuestionReq) {
        return null;
    }

    @Override
    public CommunityAnswerRes saveAnswer(CommunityAnswerReq communityAnswerReq) {
        CommunityQuestion communityQuestion = communityReader.getCommunityQuestionDetailsById(String.valueOf(communityAnswerReq.questionId()));
        CommunityAnswer communityAnswer = communityWriter.saveAnswer(communityQuestion, communityAnswerReq);

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
    public CommunityAnswerRes deleteAnswer(CommunityAnswerReq communityAnswerReq) {
        return null;
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
