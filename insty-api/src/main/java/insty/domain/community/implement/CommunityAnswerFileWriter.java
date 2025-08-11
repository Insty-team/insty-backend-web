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
    public List<FileInfo> saveAnswerFiles(CommunityAnswer answer, List<MultipartFile> addFiles) {
        saveFiles(answer, addFiles);
        return communityAnswerFileRepository.findAllByCommunityAnswerId(answer.getId()).stream()
                .map(caf -> FileInfo.from(caf.getFile(), appProperties.getDomain()))
                .toList();
    }

    /**
     * 답변 파일 업데이트 (삭제/추가 분리)
     */
    public List<FileInfo> updateAnswerFiles(CommunityAnswer answer, List<MultipartFile> addFiles, List<Long> deleteFileIds) {
        if (deleteFileIds != null && !deleteFileIds.isEmpty()) {
            communityAnswerFileRepository.deleteByAnswerIdAndFileIdIn(answer.getId(), deleteFileIds);
        }
        saveFiles(answer, addFiles);
        return communityAnswerFileRepository.findAllByCommunityAnswerId(answer.getId()).stream()
                .map(caf -> FileInfo.from(caf.getFile(), appProperties.getDomain()))
                .toList();
    }

    private List<CommunityAnswerFile> saveFiles(CommunityAnswer answer, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) return List.of();
        List<FileCreateReq> fileCreateReqs = files.stream()
                .map(file -> new FileCreateReq(file, FileContainerType.ANSWER_IMAGE, answer.getId()))
                .toList();
        List<File> savedFiles = fileWriter.saveFiles(fileCreateReqs);
        List<CommunityAnswerFile> answerFiles = savedFiles.stream()
                .map(file -> CommunityAnswerFile.create(answer, file))
                .toList();
        communityAnswerFileRepository.saveAll(answerFiles);
        return answerFiles;
    }

    /**
     * 답변과 연관된 모든 파일을 S3 및 DB에서 삭제한다.
     *
     * @param answer
     */
    public void deleteAnswerFiles(CommunityAnswer answer) {
        answer.removeAllFiles();
        communityAnswerFileRepository.deleteAllByAnswerId(answer.getId());
        fileWriter.deleteAllFile(FileContainerType.ANSWER_IMAGE, answer.getId());
    }

}