package insty.domain.community.implement;


import insty.domain.community.repository.CommunityAnswerRepository;
import insty.domain.community.repository.CommunityAnswerFileRepository;
import insty.error.CommunityErrorCode;
import insty.exception.CustomException;
import insty.model.community.CommunityAnswer;
import insty.model.community.CommunityAnswerFile;
import insty.model.user.User;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CommunityAnswerReader {
    private final CommunityAnswerRepository communityAnswerRepository;
    private final CommunityAnswerFileRepository communityAnswerFileRepository;

    /**
     * 특정 질문의 모든 답변 조회
     */
    public List<CommunityAnswer> getAllCommunityAnswersByQuestionId(Long questionId) {
        List<CommunityAnswer> answers = communityAnswerRepository.findAllDetailsWithUserByCommunityQuestionId(questionId);
        
        if (answers.isEmpty()) {
            return answers;
        }
        
        attachFileDataToAnswers(answers);
        return answers;
    }

    /**
     * 커뮤니티 답변을 페이지네이션으로 조회
     */
    public Page<CommunityAnswer> getCommunityAnswersByQuestionIdWithPagination(Long questionId, Pageable pageable) {
        Page<CommunityAnswer> answerPage = communityAnswerRepository.findAllDetailsWithUserAttachmentsByCommunityQuestionIdWithPagination(questionId, pageable);
        
        if (answerPage.isEmpty()) {
            return answerPage;
        }
        
        List<CommunityAnswer> answers = answerPage.getContent();
        attachFileDataToAnswers(answers);
        
        return new PageImpl<>(answers, pageable, answerPage.getTotalElements());
    }

    /**
     * 커뮤니티 답변 상세 조회
     */
    public CommunityAnswer getCommunityAnswerById(Long answerId) {
        CommunityAnswer answer = communityAnswerRepository.findById(answerId)
                .orElseThrow(() -> new CustomException(CommunityErrorCode.COMMUNITY_ANSWER_NOT_FOUND));

        if (answer.isDeleted()) {
            throw new CustomException(CommunityErrorCode.COMMUNITY_ANSWER_ALREADY_DELETED);
        }

        return answer;
    }

    /**
     * 삭제된 답변도 포함하여 커뮤니티 답변 조회
     */
    public CommunityAnswer getCommunityAnswerByIdIncludingDeleted(Long answerId) {
        return communityAnswerRepository.findById(answerId)
                .orElseThrow(() -> new CustomException(CommunityErrorCode.COMMUNITY_ANSWER_NOT_FOUND));
    }

    /**
     * 특정 질문에 대한 활성 답변 개수 조회
     */
    public int countActiveAnswersByQuestionId(Long questionId) {
        return communityAnswerRepository.countByCommunityQuestionIdAndIsDeletedFalse(questionId);
    }

    /**
     * 특정 질문에 대한 채택된 답변 개수 조회
     */
    public int countAcceptedAnswersByQuestionId(Long questionId) {
        return communityAnswerRepository.countAcceptedAnswersByQuestionId(questionId);
    }

    /**
     * 질문에 참여한 모든 사용자 조회
     */
    public Set<User> getParticipantsByQuestionId(Long questionId) {
        List<CommunityAnswer> answers = communityAnswerRepository.findAllByCommunityQuestionId(questionId);
        return answers.stream()
                .map(CommunityAnswer::getUser)
                .collect(Collectors.toSet());
    }
    
    /**
     * 답변 리스트에 첨부파일 데이터를 매핑
     */
    private void attachFileDataToAnswers(List<CommunityAnswer> answers) {
        List<Long> answerIds = answers.stream()
                .map(CommunityAnswer::getId)
                .toList();
        
        List<CommunityAnswerFile> attachments = communityAnswerFileRepository.findAttachmentsByAnswerIds(answerIds);
        
        Map<Long, List<CommunityAnswerFile>> attachmentMap = attachments.stream()
                .collect(Collectors.groupingBy(att -> att.getCommunityAnswer().getId()));
        
        answers.forEach(answer -> {
            List<CommunityAnswerFile> answerAttachments = attachmentMap.getOrDefault(answer.getId(), List.of());
            answer.getAttachments().clear();
            answer.getAttachments().addAll(answerAttachments);
        });
    }
}