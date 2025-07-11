package insty.domain.community.implement;

import insty.domain.common.FileCreateReq;
import insty.domain.common.FileInfo;
import insty.domain.file.implement.FileWriter;
import insty.global.property.AppProperties;
import insty.model.community.CommunityAnswer;
import insty.model.community.CommunityAnswerFile;
import insty.model.community.CommunityFile;
import insty.model.community.CommunityQuestion;
import insty.model.file.File;
import insty.model.file.FileContainerType;
import insty.s3.adapter.S3FileManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommunityFileManager {

    private final FileWriter fileWriter;
    private final CommunityWriter communityWriter;
    private final AppProperties appProperties;
    private final S3FileManager s3FileManager;

    /**
     * 질문 첨부파일 저장
     */
    public List<FileInfo> saveQuestionFiles(CommunityQuestion question, List<MultipartFile> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return List.of();
        }

        List<FileCreateReq> fileCreateReqs = attachments.stream()
                .map(file -> new FileCreateReq(
                        file,
                        FileContainerType.QUESTION_IMAGE,
                        question.getId()
                ))
                .collect(Collectors.toList());

        List<File> files = fileWriter.saveFiles(fileCreateReqs);

        // CommunityFile 생성 및 저장
        List<CommunityFile> communityFiles = files.stream()
                .map(file -> CommunityFile.create(question, file))
                .collect(Collectors.toList());

        communityWriter.saveCommunityFiles(communityFiles);

        return files.stream()
                .map(file -> FileInfo.from(file, appProperties.getDomain()))
                .collect(Collectors.toList());
    }

    /**
     * 답변 이미지 파일 저장
     */
    public List<FileInfo> saveAnswerImageFiles(CommunityAnswer answer, List<MultipartFile> imageFiles) {
        if (imageFiles == null || imageFiles.isEmpty()) {
            return List.of();
        }

        List<FileCreateReq> fileCreateReqs = imageFiles.stream()
                .map(file -> new FileCreateReq(
                        file,
                        FileContainerType.ANSWER_IMAGE,
                        answer.getId()
                ))
                .collect(Collectors.toList());

        List<File> files = fileWriter.saveFiles(fileCreateReqs);

        // CommunityAnswerFile 생성 및 저장
        List<CommunityAnswerFile> communityAnswerFiles = files.stream()
                .map(file -> CommunityAnswerFile.create(answer, file))
                .collect(Collectors.toList());

        communityWriter.saveCommunityAnswerFiles(communityAnswerFiles);

        return files.stream()
                .map(file -> FileInfo.from(file, appProperties.getDomain()))
                .collect(Collectors.toList());
    }

    /**
     * 기존 첨부파일 삭제
     */
    public void deleteQuestionFiles(List<CommunityFile> existingFiles) {
        if (existingFiles == null || existingFiles.isEmpty()) {
            return;
        }

        // S3에서 파일 삭제
        for (CommunityFile communityFile : existingFiles) {
            File file = communityFile.getFile();
            s3FileManager.delete(
                    file.getContainerType().toString(),
                    file.getContainerId().toString(),
                    file.getName()
            );
        }

        // DB에서 CommunityFile 삭제
        communityWriter.deleteCommunityFiles(existingFiles);
    }

    /**
     * 기존 답변 파일 삭제
     */
    public void deleteAnswerFiles(List<CommunityAnswerFile> existingFiles) {
        if (existingFiles == null || existingFiles.isEmpty()) {
            return;
        }

        // S3에서 파일 삭제
        for (CommunityAnswerFile communityAnswerFile : existingFiles) {
            File file = communityAnswerFile.getFile();
            s3FileManager.delete(
                    file.getContainerType().toString(),
                    file.getContainerId().toString(),
                    file.getName()
            );
        }

        // DB에서 CommunityAnswerFile 삭제
        communityWriter.deleteCommunityAnswerFiles(existingFiles);
    }

    /**
     * 파일 정보를 FileInfo로 변환
     */
    public List<FileInfo> convertToFileInfos(List<CommunityFile> communityFiles) {
        if (communityFiles == null || communityFiles.isEmpty()) {
            return List.of();
        }

        return communityFiles.stream()
                .map(communityFile -> FileInfo.from(communityFile.getFile(), appProperties.getDomain()))
                .collect(Collectors.toList());
    }

    /**
     * 답변 파일 정보를 FileInfo로 변환
     */
    public List<FileInfo> convertAnswerFilesToFileInfos(List<CommunityAnswerFile> answerFiles) {
        if (answerFiles == null || answerFiles.isEmpty()) {
            return List.of();
        }

        return answerFiles.stream()
                .map(answerFile -> FileInfo.from(answerFile.getFile(), appProperties.getDomain()))
                .collect(Collectors.toList());
    }
}