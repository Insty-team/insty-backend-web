package insty.domain.community.service;

import insty.domain.common.FileCreateReq;
import insty.domain.common.FileInfo;
import insty.domain.community.dto.*;
import insty.domain.community.implement.CommunityReader;
import insty.domain.community.implement.CommunityWriter;
import insty.domain.course.implement.CourseReader;
import insty.domain.file.implement.FileWriter;
import insty.domain.user.implement.UserReader;
import insty.domain.video.repository.VideoAnswerRepository;
import insty.domain.file.repository.FileRepository;
import insty.global.property.AppProperties;
import insty.model.community.CommunityAnswer;
import insty.model.community.CommunityAnswerFile;
import insty.model.community.CommunityFile;
import insty.model.community.CommunityQuestion;
import insty.model.course.Course;
import insty.model.file.File;
import insty.model.file.FileContainerType;
import insty.model.user.User;
import insty.model.video.VideoAnswer;
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
import java.util.UUID;
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
    private final VideoAnswerRepository videoAnswerRepository;
    private final FileRepository fileRepository;

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
                    .map(answer -> CommunityAnswerRes.create(userId, answer.getContent(), null, answer.getCreatedAt(), answer.getUpdatedAt(), answer.isAccepted()))
                    .toList();
        }
        //TODO: 수정필요




        //TODO: id가 아닌 객체 내부 데이터

        boolean isAnswered = communityQuestion.isAnswered();

        Instant createdAt = communityQuestion.getCreatedAt();
        Instant updatedAt = communityQuestion.getUpdatedAt();

        // 채택된 답변 정보 생성
        CommunityAnswerRes acceptedAnswerRes = null;
        if (communityQuestion.getAcceptedAnswer() != null) {
            CommunityAnswer acceptedAnswer = communityQuestion.getAcceptedAnswer();
            acceptedAnswerRes = CommunityAnswerRes.create(
                    acceptedAnswer.getUser().getId(),
                    acceptedAnswer.getContent(),
                    null, //TODO: 첨부파일
                    acceptedAnswer.getCreatedAt(),
                    acceptedAnswer.getUpdatedAt(),
                    acceptedAnswer.isAccepted()
            );
        }

        return CommunityQuestionRes.create(
                userId,
                courseId,
                title,
                content,
                createdAt,
                updatedAt,
                answers,
                null,
                acceptedAnswerRes
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
                fileInfos,
                null

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

        // 기존 첨부파일 목록
        List<CommunityFile> existingAttachments = prevCommunityQuestion.getAttachments();

        // 질문 내용 업데이트
        CommunityQuestion updatedQuestion = communityWriter.updateQuestion(prevCommunityQuestion, communityQuestionReq, attachments);

        // 새로운 첨부파일이 있는 경우 처리
        List<FileInfo> updatedFileInfos = null;
        if (attachments != null && !attachments.isEmpty()) {
            // 기존 첨부파일 삭제
            if (existingAttachments != null && !existingAttachments.isEmpty()) {
                deleteExistingAttachments(existingAttachments);
            }
            
            // 새로운 첨부파일 저장
            updatedFileInfos = saveCommunityFiles(updatedQuestion, attachments);
        } else {
            // 첨부파일이 없는 경우 기존 첨부파일 정보 반환
            if (existingAttachments != null && !existingAttachments.isEmpty()) {
                updatedFileInfos = existingAttachments.stream()
                        .map(communityFile -> FileInfo.from(communityFile.getFile(), appProperties.getDomain()))
                        .toList();
            }
        }

        return CommunityQuestionRes.create(
                updatedQuestion.getUser().getId(),
                updatedQuestion.getCourse().getId(),
                updatedQuestion.getTitle(),
                updatedQuestion.getContent(),
                updatedQuestion.getCreatedAt(),
                updatedQuestion.getUpdatedAt(),
                null, // TODO: 답변 리스트 추가
                updatedFileInfos,
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
                communityAnswer.getUpdatedAt(),
                communityAnswer.isAccepted()
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
                        answer.getUpdatedAt(),
                        answer.isAccepted()))
                .toList();
    }

    @Override
    public CommunityAnswerRes saveAnswer(CommunityAnswerReq communityAnswerReq, List<MultipartFile> imageFiles, UUID videoUuid) {
        String questionId = communityAnswerReq.questionId();
        Long userId = communityAnswerReq.userId();

        CommunityQuestion communityQuestion = communityReader.getCommunityQuestionDetailsById(questionId);
        User user = userReader.getUser(userId);
        CommunityAnswer communityAnswer = communityWriter.saveAnswer(communityQuestion, communityAnswerReq, user);

        saveImageFiles(communityAnswer, imageFiles);
        
        // 영상 UUID가 있는 경우 처리
        if (videoUuid != null) {
            saveVideoFile(communityAnswer, videoUuid);
        }

        return CommunityAnswerRes.create(
                userId,
                communityAnswer.getContent(),
                null,
                communityAnswer.getCreatedAt(),
                communityAnswer.getUpdatedAt(),
                communityAnswer.isAccepted()
        );

    }

    private void saveImageFiles(CommunityAnswer communityAnswer, List<MultipartFile> imageFiles) {
        if (imageFiles == null || imageFiles.isEmpty()) {
            return;
        }

        List<FileCreateReq> fileCreateReqs = imageFiles.stream()
                .map(file -> new FileCreateReq(
                        file,
                        FileContainerType.ANSWER_IMAGE,
                        communityAnswer.getId()
                )).toList();

        List<File> files = fileWriter.saveFiles(fileCreateReqs);

        List<CommunityAnswerFile> communityAnswerFiles = files.stream()
                .map(file -> CommunityAnswerFile.create(communityAnswer, file))
                .toList();

        communityWriter.saveCommunityAnswerFiles(communityAnswerFiles);
    }

    private void saveVideoFile(CommunityAnswer communityAnswer, UUID videoUuid) {
        // VideoAnswer에서 영상 정보를 가져와서 CommunityAnswerFile로 저장
        VideoAnswer videoAnswer = videoAnswerRepository.findByVideoUuid(videoUuid)
                .orElseThrow(() -> new IllegalArgumentException("해당 UUID의 영상을 찾을 수 없습니다: " + videoUuid));
        
        // VideoAnswer의 정보를 바탕으로 File 엔티티 생성
        String contentType = "video/" + videoAnswer.getExtension();
        File videoFile = File.create(
                FileContainerType.ANSWER_IMAGE, // 답변 영상용 타입 (필요시 새로운 타입 추가 가능)
                communityAnswer.getId(),
                videoAnswer.getS3Key(),
                videoAnswer.getOriginalFileName(),
                contentType,
                0 // VideoAnswer에는 size 정보가 없으므로 0으로 설정
        );
        
        // File 직접 저장
        File savedFile = fileRepository.save(videoFile);
        
        // CommunityAnswerFile 생성 및 저장
        CommunityAnswerFile communityAnswerFile = CommunityAnswerFile.create(communityAnswer, savedFile);
        communityWriter.saveCommunityAnswerFile(communityAnswerFile);
    }

    private void deleteExistingAttachments(List<CommunityFile> existingAttachments) {
        if (existingAttachments == null || existingAttachments.isEmpty()) {
            return;
        }
        
        // S3에서 파일 삭제
        for (CommunityFile communityFile : existingAttachments) {
            File file = communityFile.getFile();
            s3FileManager.delete(
                file.getContainerType().toString(),
                file.getContainerId().toString(),
                file.getName()
            );
        }
        
        // DB에서 CommunityFile 삭제
        communityWriter.deleteCommunityFiles(existingAttachments);
    }

    private void deleteExistingAnswerFiles(List<CommunityAnswerFile> existingAnswerFiles) {
        if (existingAnswerFiles == null || existingAnswerFiles.isEmpty()) {
            return;
        }
        
        // S3에서 파일 삭제
        for (CommunityAnswerFile communityAnswerFile : existingAnswerFiles) {
            File file = communityAnswerFile.getFile();
            s3FileManager.delete(
                file.getContainerType().toString(),
                file.getContainerId().toString(),
                file.getName()
            );
        }
        
        // DB에서 CommunityAnswerFile 삭제
        communityWriter.deleteCommunityAnswerFiles(existingAnswerFiles);
    }

    @Override
    public CommunityAnswerRes updateAnswer(CommunityAnswerReq communityAnswerReq, List<MultipartFile> imageFiles, UUID videoUuid) {
        String answerId = communityAnswerReq.answerId();
        String questionId = communityAnswerReq.questionId();
        Long userId = communityAnswerReq.userId();

        CommunityAnswer prevCommunityAnswer = communityReader.getCommunityAnswerById(answerId);
        CommunityAnswer updateAnswer = communityWriter.updateAnswer(prevCommunityAnswer, communityAnswerReq);

        // 기존 첨부파일 목록 가져오기
        List<CommunityAnswerFile> existingAnswerFiles = communityReader.getCommunityAnswerFilesByAnswerId(answerId);

        // 이미지 파일 업데이트
        if (imageFiles != null && !imageFiles.isEmpty()) {
            // 기존 이미지 파일 삭제
            deleteExistingAnswerFiles(existingAnswerFiles);
            // 새로운 이미지 파일 저장
            saveImageFiles(updateAnswer, imageFiles);
        }
        
        // 영상 UUID 업데이트
        if (videoUuid != null) {
            // 기존 영상 파일 삭제
            deleteExistingAnswerFiles(existingAnswerFiles);
            // 새로운 영상 파일 저장
            saveVideoFile(updateAnswer, videoUuid);
        }

        return CommunityAnswerRes.create(
                userId,
                updateAnswer.getContent(),
                null, //TODO: 첨부파일
                updateAnswer.getCreatedAt(),
                updateAnswer.getUpdatedAt(),
                updateAnswer.isAccepted()
        );
    }

    @Override
    public void deleteAnswer(String answerId) {
        CommunityAnswer communityAnswer = communityReader.getCommunityAnswerById(answerId);
        communityWriter.deleteAnswer(communityAnswer);
    }

    @Override
    public void acceptAnswer(String questionId, String answerId) {
        CommunityQuestion communityQuestion = communityReader.getCommunityQuestionDetailsById(questionId);
        CommunityAnswer communityAnswer = communityReader.getCommunityAnswerById(answerId);
        
        // 질문 작성자만 답변을 채택할 수 있도록 검증
        // TODO: 실제로는 현재 로그인한 사용자가 질문 작성자인지 확인해야 함
        // 현재는 임시로 검증 로직을 제거하고, 실제 구현시 CurrentUser 어노테이션을 사용하여 검증
        
        // 답변이 해당 질문에 속하는지 검증
        if (!communityAnswer.getCommunityQuestion().getId().equals(communityQuestion.getId())) {
            throw new IllegalArgumentException("해당 질문에 속하지 않는 답변입니다.");
        }
        
        communityWriter.acceptAnswer(communityQuestion, communityAnswer);
    }

    @Override
    public void unacceptAnswer(String questionId) {
        CommunityQuestion communityQuestion = communityReader.getCommunityQuestionDetailsById(questionId);
        communityWriter.unacceptAnswer(communityQuestion);
    }

}
