package insty.domain.community.implement;

import insty.domain.community.dto.CommunityAnswerReq;
import insty.domain.community.reposiotry.CommunityAnswerRepository;
import insty.domain.community.reposiotry.CommunityQuestionRepository;
import insty.error.CommunityErrorCode;
import insty.exception.CustomException;
import insty.model.community.CommunityAnswer;
import insty.model.community.CommunityQuestion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CommunityWriter {

    private final CommunityQuestionRepository communityQuestionRepository;
    private final CommunityAnswerRepository communityAnswerRepository;

    public CommunityAnswer saveAnswer(CommunityQuestion communityQuestion, CommunityAnswerReq communityAnswerReq) {

        CommunityAnswer communityAnswer = CommunityAnswer
                .create(
                        communityQuestion,
                        communityAnswerReq.content()
                );

        return communityAnswerRepository.save(communityAnswer);
    }

    public CommunityAnswer updateAnswer(CommunityAnswer prevCommunityAnswer, CommunityAnswerReq communityAnswerReq) {
        prevCommunityAnswer.update(communityAnswerReq.content());
        return communityAnswerRepository.save(prevCommunityAnswer);
    }
}
