package insty.domain.community.implement;

import insty.domain.common.dto.PaginationReq;
import insty.domain.common.dto.PaginationRes;
import insty.domain.community.dto.CommunityQuestionRes;
import insty.domain.community.dto.CommunityQuestionSearchFilter;
import insty.domain.community.repository.CommunityQuestionQueryRepository;
import insty.model.community.CommunityQuestion;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommunityComplexReader {
    private final CommunityQuestionQueryRepository communityQuestionQueryRepository;

    public List<CommunityQuestion> searchQuestions(PaginationReq paginationReq, CommunityQuestionSearchFilter filter, String sort) {
        return communityQuestionQueryRepository.searchQuestions(paginationReq, filter, sort);
    }

    public PaginationRes countSearchQuestions(PaginationReq paginationReq, CommunityQuestionSearchFilter filter) {
        return communityQuestionQueryRepository.countSearchQuestions(paginationReq, filter);
    }
}