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

    public void saveAnswer(CommunityAnswerReq communityAnswerReq) {
        CommunityQuestion communityQuestion = communityQuestionRepository
                .findById(communityAnswerReq.questionId())
                .orElseThrow(() -> new CustomException(CommunityErrorCode.COMMUNITY_QUESTION_NOT_FOUND));

        CommunityAnswer communityAnswer = CommunityAnswer
                .create(
                        communityQuestion,
                        communityAnswerReq.content()
                );

        communityAnswerRepository.save(communityAnswer);
    }
}
