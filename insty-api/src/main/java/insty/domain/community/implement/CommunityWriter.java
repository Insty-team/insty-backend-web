package insty.domain.community.implement;

import insty.domain.common.FileInfo;
import insty.domain.community.dto.CommunityAnswerReq;
import insty.domain.community.dto.CommunityQuestionReq;
import insty.domain.community.reposiotry.CommunityAnswerFileRepository;
import insty.domain.community.reposiotry.CommunityAnswerRepository;
import insty.domain.community.reposiotry.CommunityFileRepository;
import insty.domain.community.reposiotry.CommunityQuestionRepository;
import insty.model.community.CommunityAnswer;
import insty.model.community.CommunityAnswerFile;
import insty.model.community.CommunityFile;
import insty.model.community.CommunityQuestion;
import insty.model.course.Course;
import insty.model.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Service
@Transactional
@RequiredArgsConstructor
public class CommunityWriter {

    private final CommunityQuestionRepository communityQuestionRepository;
    private final CommunityAnswerRepository communityAnswerRepository;
    private final CommunityFileRepository communityFileRepository;
    private final CommunityAnswerFileRepository communityAnswerFileRepository;

    // TODO: 첨부파일
    public CommunityQuestion saveQuestion(CommunityQuestion communityQuestion, Course course, User user) {

        return communityQuestionRepository.save(communityQuestion);
    }

    public List<CommunityFile> saveCommunityFiles(List<CommunityFile> communityFiles) {

        return communityFileRepository.saveAll(communityFiles);

    }

    public CommunityAnswerFile saveCommunityAnswerFile(CommunityAnswerFile communityAnswerFile) {

        return communityAnswerFileRepository.save(communityAnswerFile);

    }

    public CommunityQuestion updateQuestion(CommunityQuestion prevCommunityQuestion, CommunityQuestionReq communityQuestionReq, List<MultipartFile> attachments) {
        //prevCommunityQuestion.update(communityQuestionReq.title(), communityQuestionReq.content(), attachments);
        return communityQuestionRepository.save(prevCommunityQuestion);
    }

    public void deleteQuestion(CommunityQuestion communityQuestion) {

        communityQuestionRepository.delete(communityQuestion);
    }

    public CommunityAnswer saveAnswer(CommunityQuestion communityQuestion, CommunityAnswerReq communityAnswerReq, User user) {

        CommunityAnswer communityAnswer = CommunityAnswer
                .create(
                        communityQuestion,
                        user,
                        communityAnswerReq.content()
                );

        return communityAnswerRepository.save(communityAnswer);
    }

    public CommunityAnswer updateAnswer(CommunityAnswer prevCommunityAnswer, CommunityAnswerReq communityAnswerReq) {
        prevCommunityAnswer.update(communityAnswerReq.content());
        return communityAnswerRepository.save(prevCommunityAnswer);
    }

    public void deleteAnswer(CommunityAnswer communityAnswer) {

        communityAnswerRepository.delete(communityAnswer);
    }
}
