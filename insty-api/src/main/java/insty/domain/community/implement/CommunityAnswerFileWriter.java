package insty.domain.community.implement;

import insty.domain.common.FileCreateReq;
import insty.domain.common.FileInfo;
import insty.domain.community.repository.CommunityAnswerFileRepository;
import insty.domain.file.implement.FileWriter;
import insty.global.property.AppProperties;
import insty.model.community.CommunityAnswer;
import insty.model.community.CommunityAnswerFile;
import insty.model.file.File;
import insty.model.file.FileContainerType;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class CommunityAnswerFileWriter {

    private final FileWriter fileWriter;
    private final CommunityAnswerFileRepository communityAnswerFileRepository;
    private final AppProperties appProperties;

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

        communityAnswerFileRepository.saveAll(communityAnswerFiles);

        return files.stream()
                .map(file -> FileInfo.from(file, appProperties.getDomain()))
                .collect(Collectors.toList());
    }

    public List<FileInfo> updateAnswerImageFiles(CommunityAnswer answer, List<MultipartFile> imageFiles) {
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

        communityAnswerFileRepository.saveAll(communityAnswerFiles);

        return files.stream()
                .map(file -> FileInfo.from(file, appProperties.getDomain()))
                .collect(Collectors.toList());
    }

    /**
     * 기존 답변 파일 삭제
     */
    public void deleteAnswerFiles(List<CommunityAnswerFile> existingFiles) {
        if (existingFiles == null || existingFiles.isEmpty()) {
            return;
        }
        communityAnswerFileRepository.deleteAll(existingFiles);
    }
}