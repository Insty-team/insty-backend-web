package insty.domain.community.implement;


import insty.domain.common.FileCreateReq;
import insty.domain.common.FileInfo;
import insty.domain.community.repository.CommunityQuestionFileRepository;
import insty.domain.file.implement.FileWriter;
import insty.global.property.AppProperties;
import insty.model.community.CommunityQuestion;
import insty.model.community.CommunityQuestionFile;
import insty.model.file.File;
import insty.model.file.FileContainerType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class CommunityQuestionFileWriter {

    private final FileWriter fileWriter;
    private final CommunityQuestionFileRepository communityQuestionFileRepository;
    private final AppProperties appProperties;

    /**
     * 질문에 첨부된 파일을 저장합니다.
     */
    public List<FileInfo> saveQuestionFiles(CommunityQuestion question, List<MultipartFile> addFiles) {
        saveFiles(question, addFiles);
        return communityQuestionFileRepository.findAllByCommunityQuestionId(question.getId()).stream()
                .map(cf -> FileInfo.from(cf.getFile(), appProperties.getDomain()))
                .toList();
    }

    /**
     * 첨부 파일 업데이트 (삭제&추가)
     */
    public List<FileInfo> updateQuestionFiles(CommunityQuestion question, List<MultipartFile> addFiles, List<Long> deleteFileIds) {
        if (deleteFileIds != null && !deleteFileIds.isEmpty()) {
            communityQuestionFileRepository.deleteByQuestionIdAndFileIdIn(question.getId(), deleteFileIds);
        }
        saveFiles(question, addFiles);
        return communityQuestionFileRepository.findAllByCommunityQuestionId(question.getId()).stream()
                .map(cf -> FileInfo.from(cf.getFile(), appProperties.getDomain()))
                .toList();
    }

    /**
     * 첨부 파일 저장 (공통)
     */
    private List<CommunityQuestionFile> saveFiles(CommunityQuestion question, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) return List.of();
        List<FileCreateReq> fileCreateReqs = files.stream()
                .map(file -> new FileCreateReq(file, FileContainerType.QUESTION_IMAGE, question.getId()))
                .toList();
        List<File> savedFiles = fileWriter.saveFiles(fileCreateReqs);
        List<CommunityQuestionFile> communityQuestionFiles = savedFiles.stream()
                .map(file -> CommunityQuestionFile.create(question, file))
                .toList();
        communityQuestionFileRepository.saveAll(communityQuestionFiles);
        return communityQuestionFiles;
    }

    /**
     * 질문과 연관된 모든 파일을 S3 및 DB에서 삭제한다.
     *
     * @param question
     */
    public void deleteQuestionFiles(CommunityQuestion question) {
        question.removeAllFiles();
        communityQuestionFileRepository.deleteAllByQuestionId(question.getId());

        fileWriter.deleteAllFile(FileContainerType.QUESTION_IMAGE, question.getId());
    }

}