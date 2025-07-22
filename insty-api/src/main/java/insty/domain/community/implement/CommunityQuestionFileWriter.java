package insty.domain.community.implement;


import insty.domain.common.FileCreateReq;
import insty.domain.common.FileInfo;
import insty.domain.community.repository.CommunityFileRepository;
import insty.domain.file.implement.FileWriter;
import insty.global.property.AppProperties;
import insty.model.community.CommunityFile;
import insty.model.community.CommunityQuestion;
import insty.model.file.File;
import insty.model.file.FileContainerType;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class CommunityQuestionFileWriter {

    private final FileWriter fileWriter;
    private final CommunityFileRepository communityFileRepository;
    private final AppProperties appProperties;

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

        communityFileRepository.saveAll(communityFiles);

        return files.stream()
                .map(file -> FileInfo.from(file, appProperties.getDomain()))
                .collect(Collectors.toList());
    }

    /**
     * 첨부 파일 업데이트
     */
    public List<FileInfo> updateQuestionFiles(CommunityQuestion question, List<MultipartFile> attachments) {
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

        communityFileRepository.saveAll(communityFiles);

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
        communityFileRepository.deleteAll(existingFiles);
    }
}