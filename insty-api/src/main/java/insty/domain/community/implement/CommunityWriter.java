package insty.domain.community.implement;

import insty.domain.community.dto.CommunityQuestionReq;
import insty.domain.community.reposiotry.CommunityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CommunityWriter {

    private final CommunityRepository communityRepository;

    public void saveAnswer(CommunityQuestionReq communityQuestionReq) {
    }
}
