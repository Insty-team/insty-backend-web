package insty.domain.community.implement;

import insty.domain.community.repository.CommunityQuestionRepository;
import insty.error.CommunityErrorCode;
import insty.exception.CustomException;
import insty.model.community.CommunityQuestion;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CommunityQuestionReader {

    private final CommunityQuestionRepository communityQuestionRepository;

    public List<CommunityQuestion> getAllCommunityQuestions() {
        return communityQuestionRepository.findAll();
    }

    /**
     * 특정 강좌의 모든 커뮤니티 질문 조회
     */
    public List<CommunityQuestion> getAllCommunityQuestionsByCourseId(Long courseId) {
        return communityQuestionRepository.findAllByCourseId(courseId);
    }

    /**
     * 커뮤니티 질문 상세조회
     */
    public CommunityQuestion getCommunityQuestionDetailsById(Long questionId) {
        return  communityQuestionRepository.findById(questionId)
                .orElseThrow(() -> new CustomException(CommunityErrorCode.COMMUNITY_QUESTION_NOT_FOUND));
    }
}