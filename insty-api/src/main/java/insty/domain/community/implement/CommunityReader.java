package insty.domain.community.implement;

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
public class CommunityReader {

    private final CommunityQuestionRepository communityQuestionRepository;
    private final CommunityAnswerRepository communityAnswerRepository;

    //id로 질문 상세 조회
    public CommunityQuestion getCommunityQuestionDetailsById(String questionId) {
        return  communityQuestionRepository.getCommunityQuestion(Long.parseLong(questionId))
                .orElseThrow(() -> new CustomException(CommunityErrorCode.COMMUNITY_QUESTION_NOT_FOUND));
    }

    public CommunityAnswer getCommunityAnswerById(String answerId) {
        return communityAnswerRepository.getCommunityAnswer(Long.parseLong(answerId))
                .orElseThrow(() -> new CustomException(CommunityErrorCode.COMMUNITY_ANSWER_NOT_FOUND));
    }
}
