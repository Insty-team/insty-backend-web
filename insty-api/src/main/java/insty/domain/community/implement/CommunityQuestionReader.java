package insty.domain.community.implement;

import insty.domain.common.dto.PaginationReq;
import insty.domain.common.dto.PaginationRes;
import insty.domain.community.dto.CommunityQuestionSearchFilter;
import insty.domain.community.repository.CommunityQuestionQueryRepository;
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
    private final CommunityQuestionQueryRepository communityQuestionQueryRepository;

    /**
     * 필터, 검색 조건, 정렬을 기준으로 검색
     */
    public List<CommunityQuestion> searchQuestions(PaginationReq paginationReq, CommunityQuestionSearchFilter filter, String sort) {
        return communityQuestionQueryRepository.searchQuestions(paginationReq, filter, sort);
    }

    /**
     * 총 검색 개수
     */
    public PaginationRes countSearchQuestions(PaginationReq paginationReq, CommunityQuestionSearchFilter filter) {
        return communityQuestionQueryRepository.countSearchQuestions(paginationReq, filter);
    }

    /**
     * 모든 커뮤니티 질문 조회 (가급적 쓰지 말것)
     * @return
     */
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
        // todo : 관련 엔티티까지 전부 조회, N+1문제 방지
        //  (단 Answer는 join 제외 : Why? answerService를 사용하면 되고, fetch join을 한다해도 6중 join으로 복잡하고 메모리 사용량도 늘어남)
        CommunityQuestion question = communityQuestionRepository.findWithCourseUserAttachmentsById(questionId)
                .orElseThrow(() -> new CustomException(CommunityErrorCode.COMMUNITY_QUESTION_NOT_FOUND));

        // 삭제된 질문인지 검증
        if (question.isDeleted()) {
            throw new CustomException(CommunityErrorCode.COMMUNITY_QUESTION_ALREADY_DELETED);
        }

        return question;
    }

    /**
     * 삭제된 질문을 포함한 커뮤니티 질문 상세조회
     */
    public CommunityQuestion getCommunityQuestionDetailsByIdIncludingDeleted(Long questionId) {
        return communityQuestionRepository.findById(questionId)
                .orElseThrow(() -> new CustomException(CommunityErrorCode.COMMUNITY_QUESTION_NOT_FOUND));
    }
}