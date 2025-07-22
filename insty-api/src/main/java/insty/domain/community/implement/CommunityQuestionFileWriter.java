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
     * 질문에 첨부된 파일을 저장합니다.
     */
    public List<FileInfo> saveQuestionFiles(CommunityQuestion question, List<MultipartFile> addFiles) {
        saveFiles(question, addFiles);
        return communityFileRepository.findAllByCommunityQuestionId(question.getId()).stream()
                .map(cf -> FileInfo.from(cf.getFile(), appProperties.getDomain()))
                .toList();
    }

    /**
     * 첨부 파일 업데이트 (삭제&추가)
     */
    public List<FileInfo> updateQuestionFiles(CommunityQuestion question, List<MultipartFile> addFiles, List<Long> deleteFileIds) {
        // 삭제
        if (deleteFileIds != null && !deleteFileIds.isEmpty()) {
            communityFileRepository.deleteByQuestionIdAndFileIdIn(question.getId(), deleteFileIds);
        }
        // 추가
        saveFiles(question, addFiles);
        // 최종 파일 정보 반환
        return communityFileRepository.findAllByCommunityQuestionId(question.getId()).stream()
                .map(cf -> FileInfo.from(cf.getFile(), appProperties.getDomain()))
                .toList();
    }

    /**
     * 첨부 파일 저장 (공통)
     */
    private List<CommunityFile> saveFiles(CommunityQuestion question, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) return List.of();
        List<FileCreateReq> fileCreateReqs = files.stream()
                .map(file -> new FileCreateReq(file, FileContainerType.QUESTION_IMAGE, question.getId()))
                .toList();
        List<File> savedFiles = fileWriter.saveFiles(fileCreateReqs);
        List<CommunityFile> communityFiles = savedFiles.stream()
                .map(file -> CommunityFile.create(question, file))
                .toList();
        communityFileRepository.saveAll(communityFiles);
        return communityFiles;
    }
}