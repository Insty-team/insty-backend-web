package insty.domain.community.implement;

import insty.domain.community.dto.CommunityAnswerCreateReq;
import insty.domain.community.dto.CommunityAnswerUpdateReq;
import insty.domain.community.repository.CommunityAnswerFileRepository;
import insty.domain.community.repository.CommunityAnswerRepository;
import insty.error.CommunityErrorCode;
import insty.exception.CustomException;
import insty.model.community.CommunityAnswer;
import insty.model.community.CommunityAnswerFile;
import insty.model.community.CommunityQuestion;
import insty.model.user.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CommunityAnswerWriter {

    private final CommunityAnswerRepository communityAnswerRepository;
    private final CommunityAnswerFileRepository communityAnswerFileRepository;


    /**
     * 커뮤니티 답변 생성 및 저장
     */
    public CommunityAnswer saveAnswer(User user, CommunityQuestion question, CommunityAnswerCreateReq req) {
        CommunityAnswer answer = CommunityAnswer.create(question, user, req.content());
        return communityAnswerRepository.save(answer);
    }

    /**
     * 커뮤니티 답변 수정 (id로 직접 조회)
     */
    public CommunityAnswer updateAnswer(Long answerId, CommunityAnswerUpdateReq req) {
        CommunityAnswer answer = communityAnswerRepository.findById(answerId)
                .orElseThrow(() -> new CustomException(CommunityErrorCode.COMMUNITY_ANSWER_NOT_FOUND));
        answer.update(req.content());
        return communityAnswerRepository.save(answer);
    }

    /**
     * 커뮤니티 답변 삭제
     */
    public void deleteAnswer(CommunityAnswer communityAnswer) {
        communityAnswerRepository.delete(communityAnswer);
    }

    /**
     * 커뮤니티 답변 파일 목록 저장
     */
    public List<CommunityAnswerFile> saveCommunityAnswerFiles(List<CommunityAnswerFile> communityAnswerFiles) {
        return communityAnswerFileRepository.saveAll(communityAnswerFiles);
    }

    /**
     * 커뮤니티 답변 파일 저장
     */
    public CommunityAnswerFile saveCommunityAnswerFile(CommunityAnswerFile communityAnswerFile) {
        return communityAnswerFileRepository.save(communityAnswerFile);
    }

    /**
     * 커뮤니티 답변 파일 목록 삭제
     */
    public void deleteCommunityAnswerFiles(List<CommunityAnswerFile> communityAnswerFiles) {
        communityAnswerFileRepository.deleteAll(communityAnswerFiles);
    }
}
