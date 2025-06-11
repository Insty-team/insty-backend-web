package insty.domain.community.service;

import insty.domain.common.FileCreateReq;
import insty.domain.common.FileInfo;
import insty.domain.community.dto.*;
import insty.domain.community.implement.CommunityReader;
import insty.domain.community.implement.CommunityWriter;
import insty.domain.course.implement.CourseReader;
import insty.domain.file.implement.FileWriter;
import insty.domain.user.implement.UserReader;
import insty.global.property.AppProperties;
import insty.model.community.CommunityAnswer;
import insty.model.community.CommunityFile;
import insty.model.community.CommunityQuestion;
import insty.model.course.Course;
import insty.model.file.File;
import insty.model.file.FileContainerType;
import insty.model.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CommunityServiceImpl implements CommunityService {

    private final CommunityReader communityReader;
    private final CommunityWriter communityWriter;
    private final CourseReader courseReader;
    private final UserReader userReader;
    private final FileWriter fileWriter;
    private final AppProperties appProperties;

    @Override
    public CommunityQuestionRes getQuestionDetails(String questionId) {
        CommunityQuestion communityQuestion = communityReader.getCommunityQuestionDetailsById(questionId);
        List<CommunityAnswer> communityAnswers = communityQuestion.getAnswers();
        List<CommunityFile> communityAttactments = communityQuestion.getAttachments();

        List<CommunityAnswerRes> answers = new ArrayList<>();

        if (communityAnswers != null) {
            answers = communityAnswers.stream()
                    .map(answer -> CommunityAnswerRes.create(answer.getContent()))
                    .toList();
        }
        //TODO: 수정필요
        List<CommunityAttachmentRes> attachments = getCommunityAttachments(communityAttactments);

        String title = communityQuestion.getTitle();
        String content = communityQuestion.getContent();

        User user = communityQuestion.getUser();
        Long userId = user.getId();

        Course course = communityQuestion.getCourse();
        Long courseId = course.getId();

        //TODO: id가 아닌 객체 내부 데이터

        boolean isAnswered = communityQuestion.isAnswered();

        Instant createdAt = communityQuestion.getCreatedAt();
        Instant updatedAt = communityQuestion.getUpdatedAt();

        return CommunityQuestionRes.create(
                userId,
                courseId,
                title,
                content,
                createdAt,
                updatedAt,
                answers
                //attachments
        );
    }


    List<CommunityAttachmentRes> getCommunityAttachments(List<CommunityFile> communityAttactments) {
        List<CommunityAttachmentRes> attacments = new ArrayList<>();

        for (CommunityFile attachment : communityAttactments) {
            File communityFile = attachment.getFile();
//            FileContainerType fileContainerType = communityFile.getContainerType();
//            String contentType = communityFile.getContentType();
//            String fileContent = attachment.getFileContent();
//
//            CommunityAttachmentRes communityAttachmentRes = CommunityAttachmentRes.create(
//                    fileContainerType,
//                    contentType,
//                    fileContent
//            );

//            attacments.add(communityAttachmentRes);
        }

        return attacments;
    }

    @Override
    public List<CommunityQuestionRes> getAllQuestions() {
        List<CommunityQuestion> communityQuestions = communityReader.getAllCommunityQuestions();

        return communityQuestions.stream()
                .map(question -> CommunityQuestionRes.create(
                        null,
                        null,
                        question.getTitle(),
                        question.getContent(),
                        question.getCreatedAt(),
                        question.getUpdatedAt(),
                        null
                )).toList();
    }

    @Override
    public List<CommunityQuestionRes> getQuestionsByCourseId(String courseId) {
        List<CommunityQuestion> communityQuestions = communityReader.getAllCommunityQuestionsByCourseId(courseId);

        return communityQuestions.stream()
                .map(question -> CommunityQuestionRes.create(
                        null,
                        null,
                        question.getTitle(),
                        question.getContent(),
                        question.getCreatedAt(),
                        question.getUpdatedAt(),
                        null
                )).toList();
    }

    @Override
    public CommunityQuestionRes saveQuestion(CommunityQuestionReq communityQuestionReq, List<MultipartFile> attachments) {
        Course course = courseReader.getCourseById(communityQuestionReq.courseId());
        User user = userReader.getUser(communityQuestionReq.userId());
        // 커뮤니티 질문 생성
        CommunityQuestion communityQuestion = CommunityQuestion
                .create(
                        course,
                        user,
                        communityQuestionReq.title(),
                        communityQuestionReq.content()
                );

        communityQuestion = communityWriter.saveQuestion(communityQuestion, course, user);
        List<FileInfo> fileInfos = saveCommunityFiles(communityQuestion, attachments); //첨부파일 리스트

        return CommunityQuestionRes.create(
                user.getId(),
                course.getId(),
                communityQuestion.getTitle(),
                communityQuestion.getContent(),
                Instant.now(),
                Instant.now(),
                null //TODO: 답변 리스트 추가
        );
    }

    public List<FileInfo> saveCommunityFiles(CommunityQuestion communityQuestion, List<MultipartFile> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return null;
        }
        List<FileCreateReq> fileCreateReqs = attachments.stream()
                .map(file -> new FileCreateReq(
                        file,
                        FileContainerType.QUESTION_IMAGE,
                        communityQuestion.getId()
                )).toList();

        List<File> files = fileWriter.saveFiles(fileCreateReqs);
//        List<CommunityFile> communityFiles = createCommunityAttachments(attachments, communityQuestion);
        List<CommunityFile> communityFiles = files.stream()
                .map(file -> CommunityFile.create(communityQuestion, file))
                .toList();

        communityWriter.saveCommunityFiles(communityFiles);

        return files.stream()
                .map(file -> FileInfo.from(file, appProperties.getDomain()))
                .toList();

    }

    public List<CommunityFile> createCommunityAttachments(List<MultipartFile> attachments, CommunityQuestion communityQuestion) {
        if (attachments == null || attachments.isEmpty()) {
            return new ArrayList<>();
        }
        List<FileCreateReq> fileCreateReqs = new ArrayList<>();

        for (MultipartFile attachment : attachments) {
            FileCreateReq fileCreateReq = new FileCreateReq(
                    attachment,
                    FileContainerType.QUESTION_IMAGE,
                    communityQuestion.getId()
            );
            fileCreateReqs.add(fileCreateReq);
        }

        List<File> files = fileWriter.saveFiles(fileCreateReqs);

        List<CommunityFile> communityAttachments = files
                .stream()
                .map(
                        file -> CommunityFile.create(
                                communityQuestion,
                                file
                        )
                ).toList();

        return communityAttachments;
    }

    @Override
    public CommunityQuestionRes updateQuestion(CommunityQuestionReq communityQuestionReq) {
        CommunityQuestion prevCommunityQuestion = communityReader.getCommunityQuestionDetailsById(String.valueOf(communityQuestionReq.questionId()));
        CommunityQuestion updatedQuestion = communityWriter.updateQuestion(prevCommunityQuestion, communityQuestionReq);
        //TODO: 첨부파일 추가
        return CommunityQuestionRes.create(
                null,
                null,
                updatedQuestion.getTitle(),
                updatedQuestion.getContent(),
                null,
                Instant.now(),
                null //TODO: 답변 리스트 추가
        );
    }

    @Override
    public void deleteQuestion(String questionId) {
        CommunityQuestion communityQuestion = communityReader.getCommunityQuestionDetailsById(String.valueOf(questionId));
        communityWriter.deleteQuestion(communityQuestion);
    }

    @Override
    public List<CommunityAnswerRes> getAllAnswers(String questionId) {
        List<CommunityAnswer> communityAnswers = new ArrayList<CommunityAnswer>(); //communityReader.getAllCommunityAnswers(questionId);

        return communityAnswers.stream()
                .map(answer -> CommunityAnswerRes.create(
                        answer.getContent()))
                .toList();
    }

    @Override
    public CommunityAnswerRes saveAnswer(CommunityAnswerReq communityAnswerReq) {
        CommunityQuestion communityQuestion = communityReader.getCommunityQuestionDetailsById(String.valueOf(communityAnswerReq.questionId()));
        User user = userReader.getUser(communityAnswerReq.userId());
        CommunityAnswer communityAnswer = communityWriter.saveAnswer(communityQuestion, communityAnswerReq, user);

        return CommunityAnswerRes.create(
                communityAnswer.getContent()
        );

    }

    @Override
    public CommunityAnswerRes updateAnswer(CommunityAnswerReq communityAnswerReq) {
        CommunityAnswer prevCommunityAnswer = communityReader.getCommunityAnswerById(String.valueOf(communityAnswerReq.answerId()));
        CommunityAnswer updateAnswer = communityWriter.updateAnswer(prevCommunityAnswer, communityAnswerReq);

        return CommunityAnswerRes.create(
                updateAnswer.getContent()
        );
    }

    @Override
    public void deleteAnswer(CommunityAnswerReq communityAnswerReq) {
        CommunityAnswer communityAnswer = communityReader.getCommunityAnswerById(String.valueOf(communityAnswerReq.answerId()));
        communityWriter.deleteAnswer(communityAnswer);
    }

    @Override
    public CommunityAnswerRes getAIAnswerRecommendation(CommunityAnswerReq communityAnswerReq) {
        return null;
    }

    @Override
    public CommunityAnswerRes postAnswerImage(CommunityAnswerReq communityAnswerReq) {
        return null;
    }

    @Override
    public CommunityAnswerReq postAnswerVideo(CommunityAnswerReq communityAnswerReq) {
        return null;
    }
}
