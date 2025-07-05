package insty.domain.community.service;

import insty.domain.community.dto.CommunityAnswerReq;
import insty.domain.community.dto.CommunityAnswerRes;
import insty.domain.community.dto.CommunityQuestionReq;
import insty.domain.community.dto.CommunityQuestionRes;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface CommunityService {

    CommunityQuestionRes getQuestionDetails(String questionId);

    List<CommunityQuestionRes> getQuestionsByCourseId(String courseId);

    List<CommunityQuestionRes> getAllQuestions();

    CommunityQuestionRes saveQuestion(CommunityQuestionReq communityQuestionReq, List<MultipartFile> attachments);

    CommunityQuestionRes updateQuestion(CommunityQuestionReq communityQuestionReq, List<MultipartFile> attachments);

    void deleteQuestion(String questionId);

    CommunityAnswerRes getAnswerDetails(String answerId);

    //불필요해보임
    List<CommunityAnswerRes> getAllAnswers(String questionId);

    CommunityAnswerRes saveAnswer(CommunityAnswerReq communityAnswerReq, List<MultipartFile> imageFiles, UUID videoUuid);

    CommunityAnswerRes updateAnswer(CommunityAnswerReq communityAnswerReq, List<MultipartFile> imageFiles, UUID videoUuid);

    void deleteAnswer(String answerId);
}
