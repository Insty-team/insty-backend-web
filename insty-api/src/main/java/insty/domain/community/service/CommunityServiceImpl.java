package insty.domain.community.service;

import insty.domain.community.dto.CommunityQuestionReq;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CommunityServiceImpl implements CommunityService {

    @Override
    public CommunityQuestionReq getQuestionDetails(String questionId) {
        return null;
    }
}
