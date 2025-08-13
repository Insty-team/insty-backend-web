package insty.domain.community.implement;

import insty.domain.community.dto.CommunityQuestionCreateReq;
import insty.domain.community.dto.CommunityQuestionUpdateReq;
import insty.domain.community.repository.CommunityQuestionRepository;
import insty.error.CommunityErrorCode;
import insty.exception.CustomException;
import insty.model.community.CommunityQuestion;
import insty.model.course.Course;
import insty.model.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CommunityQuestionWriter {

    private final CommunityQuestionRepository communityQuestionRepository;

    /**
     * 커뮤니티 질문 생성 및 저장
     */
    public CommunityQuestion saveQuestion(User user, Course course, CommunityQuestionCreateReq req) {
        CommunityQuestion question = CommunityQuestion.create(course, user, req.title(), req.content());
        return communityQuestionRepository.save(question);
    }

    /**
     * 커뮤니티 질문 수정 (id로 직접 조회)
     */
    public CommunityQuestion updateQuestion(Long questionId, CommunityQuestionUpdateReq req) {
        CommunityQuestion question = communityQuestionRepository.findById(questionId)
                .orElseThrow(() -> new CustomException(CommunityErrorCode.COMMUNITY_QUESTION_NOT_FOUND));
        if (question.isDeleted()) {
            throw new CustomException(CommunityErrorCode.COMMUNITY_QUESTION_ALREADY_DELETED);
        }
        question.update(req.title(), req.content(), question.getAttachments());
        return communityQuestionRepository.save(question);
    }

    /**
     * 커뮤니티 질문 삭제
     */
    public void deleteQuestion(CommunityQuestion communityQuestion) {
        if (communityQuestion.isDeleted()) {
            throw new CustomException(CommunityErrorCode.COMMUNITY_QUESTION_ALREADY_DELETED);
        }
        communityQuestionRepository.delete(communityQuestion);
    }

}
