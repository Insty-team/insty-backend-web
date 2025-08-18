package insty.domain.community.implement;

import insty.domain.common.dto.PaginationReq;
import insty.domain.common.dto.PaginationRes;
import insty.domain.community.dto.CommunityQuestionSearchFilter;
import insty.domain.community.dto.CommunityQuestionSearchInfo;
import insty.domain.community.repository.CommunityQuestionQueryRepository;
import insty.domain.community.repository.CommunityQuestionRepository;
import insty.error.CommunityErrorCode;
import insty.exception.CustomException;
import insty.model.community.CommunityQuestion;
import java.util.List;
import java.util.Map;
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
    public List<CommunityQuestionSearchInfo> searchQuestions(PaginationReq paginationReq, CommunityQuestionSearchFilter filter, String sort) {
        return communityQuestionQueryRepository.searchQuestions(paginationReq, filter, sort);
    }

    /**
     * 총 검색 개수
     */
    public PaginationRes countSearchQuestions(PaginationReq paginationReq, CommunityQuestionSearchFilter filter) {
        return communityQuestionQueryRepository.countSearchQuestions(paginationReq, filter);
    }

    /**
     * 질문 ID 리스트에 해당하는 답변 개수를 조회
     */
    public Map<Long, Long> getAnswerCountsByQuestionIds(List<Long> questionIds) {
        return communityQuestionQueryRepository.getAnswerCountsByQuestionIds(questionIds);
    }

    /**
     * 모든 커뮤니티 질문 조회 (가급적 쓰지 말것)
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
     * 커뮤니티 질문과 첨부파일을 포함한 결과
     * (파일 포함 & 질문 미포함)
     */
    public CommunityQuestion getCommunityQuestionWithFilesById(Long questionId) {
        CommunityQuestion question = communityQuestionRepository.findDetailsWithUserAttachmentsById(questionId)
                .orElseThrow(() -> new CustomException(CommunityErrorCode.COMMUNITY_QUESTION_NOT_FOUND));
        if (question.isDeleted()) {
            throw new CustomException(CommunityErrorCode.COMMUNITY_QUESTION_ALREADY_DELETED);
        }
        return question;
    }

    /**
     * 커뮤니티 질문과 답변 리스트를 포함한 결과
     * (파일 미포함 & 질문 미포함 - 질문 파일은 미포함)
     */
    public CommunityQuestion getCommunityQuestionWithAnswerById(Long questionId){
        CommunityQuestion question = communityQuestionRepository.findDetailsWithUserAttachmentsById(questionId)
                .orElseThrow(() -> new CustomException(CommunityErrorCode.COMMUNITY_QUESTION_NOT_FOUND));
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