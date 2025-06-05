package insty.domain.community.implement;

import insty.domain.community.dto.CommunityAnswerReq;
import insty.domain.community.dto.CommunityQuestionReq;
import insty.domain.community.reposiotry.CommunityAnswerRepository;
import insty.domain.community.reposiotry.CommunityQuestionRepository;
import insty.error.CommunityErrorCode;
import insty.exception.CustomException;
import insty.model.community.CommunityAnswer;
import insty.model.community.CommunityQuestion;
import insty.model.course.Course;
import insty.model.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CommunityWriter {

    private final CommunityQuestionRepository communityQuestionRepository;
    private final CommunityAnswerRepository communityAnswerRepository;

    public CommunityQuestion saveQuestion(CommunityQuestionReq communityQuestionReq, Course course, User user) {


        CommunityQuestion communityQuestion = CommunityQuestion
                .create(
                        course,
                        user,
                        communityQuestionReq.title(),
                        communityQuestionReq.content()
                );

        return communityQuestionRepository.save(communityQuestion);
    }

    public CommunityAnswer saveAnswer(CommunityQuestion communityQuestion, CommunityAnswerReq communityAnswerReq, User user) {

        CommunityAnswer communityAnswer = CommunityAnswer
                .create(
                        communityQuestion,
                        user,
                        communityAnswerReq.content()
                );

        return communityAnswerRepository.save(communityAnswer);
    }

    public CommunityAnswer updateAnswer(CommunityAnswer prevCommunityAnswer, CommunityAnswerReq communityAnswerReq) {
        prevCommunityAnswer.update(communityAnswerReq.content());
        return communityAnswerRepository.save(prevCommunityAnswer);
    }

    public void deleteAnswer(CommunityAnswer communityAnswer) {

        communityAnswerRepository.delete(communityAnswer);
    }
}
