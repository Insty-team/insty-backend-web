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
import insty.model.community.CommunityAnswerFile;
import insty.model.community.CommunityFile;
import insty.model.community.CommunityQuestion;
import insty.model.course.Course;
import insty.model.file.File;
import insty.model.file.FileContainerType;
import insty.model.user.User;
import insty.s3.adapter.S3FileManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    private final S3FileManager s3FileManager;

    @Override
    public CommunityQuestionRes getQuestionDetails(String questionId) {
        CommunityQuestion communityQuestion = communityReader.getCommunityQuestionDetailsById(questionId);
        List<CommunityAnswer> communityAnswers = communityQuestion.getAnswers();
        List<CommunityFile> communityAttactments = communityQuestion.getAttachments();

        String title = communityQuestion.getTitle();
        String content = communityQuestion.getContent();

        User user = communityQuestion.getUser();
        Long userId = user.getId();

        Course course = communityQuestion.getCourse();
        Long courseId = course.getId();

        List<CommunityAnswerRes> answers = new ArrayList<>();

        //TODO: 댓글 이미지
        if (communityAnswers != null) {
            answers = communityAnswers.stream()
                    .map(answer -> CommunityAnswerRes.create(userId, answer.getContent(), null, answer.getCreatedAt(), answer.getUpdatedAt()))
                    .toList();
        }
        //TODO: 수정필요




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
                answers,
                null
                //attachments
        );
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
                        null,
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
                        null,
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
                null,
                fileInfos

        );
    }

    private List<FileInfo> saveCommunityFiles(CommunityQuestion communityQuestion, List<MultipartFile> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return null;
        }
        List<FileCreateReq> fileCreateReqs = attachments.stream()
                .map(file -> new FileCreateReq(
                        file,
                        FileContainerType.QUESTION_IMAGE,
                        communityQuestion.getId()
                )).toList();

//        List<File> files = fileCreateReqs.stream()
//                .map(fileCreateReq -> uploadAndCreateFile(fileCreateReq))
//                .toList();
        List<File> files = fileWriter.saveFiles(fileCreateReqs);


        List<CommunityFile> communityFiles = files.stream()
                .map(file -> CommunityFile.create(communityQuestion, file))
                .toList();

        communityWriter.saveCommunityFiles(communityFiles);

        return files.stream()
                .map(file -> FileInfo.from(file, appProperties.getDomain()))
                .toList();

    }

    private File uploadAndCreateFile(FileCreateReq req) {
        String uploadName = s3FileManager.upload(req.file(), req.containerType().toString(),
                req.containerId().toString());
        return File.create(req.containerType(), req.containerId(), uploadName, req.file().getOriginalFilename(),
                req.file().getContentType(), req.file().getSize());
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
    public CommunityQuestionRes updateQuestion(CommunityQuestionReq communityQuestionReq, List<MultipartFile> attachments) {
        CommunityQuestion prevCommunityQuestion = communityReader.getCommunityQuestionDetailsById(String.valueOf(communityQuestionReq.questionId()));

        //새 첨푸파일과 기존 첨부파일 비교
        List<CommunityFile> existingAttachments = prevCommunityQuestion.getAttachments();

        /*
        // 기존 첨부파일 ID 목록
        Set<Long> existingFileIds = existingAttachments.stream()
                .map(CommunityFile::getId)
                .collect(Collectors.toSet());

        // DTO에서 전달된 첨부파일 ID 목록 (예: List<Long> attachmentIds)
        Set<Long> newFileIds = new HashSet<>(dto.getAttachmentIds());

        // 삭제 대상: 기존에는 있지만, 새 목록에는 없는 파일
        Set<Long> toDelete = new HashSet<>(existingFileIds);
        toDelete.removeAll(newFileIds);

        // 추가 대상: 새 목록에는 있지만, 기존에는 없는 파일
        Set<Long> toAdd = new HashSet<>(newFileIds);
        toAdd.removeAll(existingFileIds);

        // 삭제 처리
        for (Long fileId : toDelete) {
            fileWriter.deleteFileById(fileId);
        }

        // 추가 처리 (예: MultipartFile로 전달된 신규 파일들)
        for (MultipartFile file : dto.getNewFiles()) {
            // 파일 업로드 및 DB 저장
            FileCreateReq req = new FileCreateReq(file, FileContainerType.QUESTION_IMAGE, questionId);
            File savedFile = uploadAndCreateFile(req);
            // CommunityFile로 매핑 및 저장
            CommunityFile communityFile = CommunityFile.create(communityQuestion, savedFile);
            communityWriter.saveCommunityFile(communityFile);
        }

         */

        //TODO: 첨부파일
        CommunityQuestion updatedQuestion = communityWriter.updateQuestion(prevCommunityQuestion, communityQuestionReq, attachments);
        //TODO: 첨부파일 추가
        return CommunityQuestionRes.create(
                null,
                null,
                updatedQuestion.getTitle(),
                updatedQuestion.getContent(),
                null,
                Instant.now(),
                null, //TODO: 답변 리스트 추가
                null
        );
    }

    @Override
    public void deleteQuestion(String questionId) {
        CommunityQuestion communityQuestion = communityReader.getCommunityQuestionDetailsById(String.valueOf(questionId));
        communityWriter.deleteQuestion(communityQuestion);
    }

    @Override
    public CommunityAnswerRes getAnswerDetails(String answerId) {
        CommunityAnswer communityAnswer = communityReader.getCommunityAnswerById(answerId);
        User user = communityAnswer.getUser();

        return CommunityAnswerRes.create(
                user.getId(),
                communityAnswer.getContent(),
                null, //TODO: 첨부파일
                communityAnswer.getCreatedAt(),
                communityAnswer.getUpdatedAt()
        );
    }

    @Override
    public List<CommunityAnswerRes> getAllAnswers(String questionId) {

        List<CommunityAnswer> communityAnswers = communityReader.getAllCommunityAnswers(questionId);

        return communityAnswers.stream()
                .map(answer -> CommunityAnswerRes.create(
                        answer.getUser().getId(),
                        answer.getContent(),
                        null, //TODO: 첨부파일
                        answer.getCreatedAt(),
                        answer.getUpdatedAt()))
                .toList();
    }

    @Override
    public CommunityAnswerRes saveAnswer(CommunityAnswerReq communityAnswerReq, MultipartFile imageFile) {
        String questionId = communityAnswerReq.questionId();
        Long userId = communityAnswerReq.userId();

        CommunityQuestion communityQuestion = communityReader.getCommunityQuestionDetailsById(questionId);
        User user = userReader.getUser(userId);
        CommunityAnswer communityAnswer = communityWriter.saveAnswer(communityQuestion, communityAnswerReq, user);

        FileCreateReq fileCreateReq = new FileCreateReq(
                        imageFile,
                        FileContainerType.QUESTION_IMAGE,
                        communityAnswer.getId()
                );

        File file = fileWriter.saveFile(fileCreateReq);
        CommunityAnswerFile communityAnswerFile = CommunityAnswerFile.create(communityAnswer, file);
        communityWriter.saveCommunityAnswerFile(communityAnswerFile);

        return CommunityAnswerRes.create(
                userId,
                communityAnswer.getContent(),
                null,
                communityAnswer.getCreatedAt(),
                communityAnswer.getUpdatedAt()
        );

    }

    @Override
    public CommunityAnswerRes updateAnswer(CommunityAnswerReq communityAnswerReq) {
        String answerId = communityAnswerReq.answerId();
        String questionId = communityAnswerReq.questionId();
        Long userId = communityAnswerReq.userId();

        CommunityAnswer prevCommunityAnswer = communityReader.getCommunityAnswerById(answerId);
        CommunityAnswer updateAnswer = communityWriter.updateAnswer(prevCommunityAnswer, communityAnswerReq);

        return CommunityAnswerRes.create(
                userId,
                updateAnswer.getContent(),
                null, //TODO: 첨부파일
                updateAnswer.getCreatedAt(),
                updateAnswer.getUpdatedAt()
        );
    }

    @Override
    public void deleteAnswer(CommunityAnswerReq communityAnswerReq) {
        CommunityAnswer communityAnswer = communityReader.getCommunityAnswerById(String.valueOf(communityAnswerReq.answerId()));
        communityWriter.deleteAnswer(communityAnswer);
    }

}
