package insty.domain.community.implement;

import insty.domain.community.reposiotry.CommunityAnswerRepository;
import insty.domain.community.reposiotry.CommunityQuestionRepository;
import insty.error.CommunityErrorCode;
import insty.error.CourseErrorCode;
import insty.exception.CustomException;
import insty.model.community.CommunityAnswer;
import insty.model.community.CommunityQuestion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CommunityReader {

    private final CommunityQuestionRepository communityQuestionRepository;
    private final CommunityAnswerRepository communityAnswerRepository;

    public List<CommunityQuestion> getAllCommunityQuestions() {
        return communityQuestionRepository.findAll();
    }

    public List<CommunityQuestion> getAllCommunityQuestionsByCourseId(String courseId) {
        return communityQuestionRepository.findAllByCourseId(Long.parseLong(courseId));
    }

    //id로 질문 상세 조회
    public CommunityQuestion getCommunityQuestionDetailsById(String questionId) {
        return  communityQuestionRepository.findById(Long.parseLong(questionId))
                .orElseThrow(() -> new CustomException(CommunityErrorCode.COMMUNITY_QUESTION_NOT_FOUND));
    }

    public CommunityAnswer getCommunityAnswerById(String answerId) {
        return communityAnswerRepository.findById(Long.parseLong(answerId))
                .orElseThrow(() -> new CustomException(CommunityErrorCode.COMMUNITY_ANSWER_NOT_FOUND));
    }

//    public List<CommunityAnswer> getAllCommunityAnswers(String questionId) {
//        return communityAnswerRepository.findAllByQuestionId(Long.parseLong(questionId));
//    }
}
