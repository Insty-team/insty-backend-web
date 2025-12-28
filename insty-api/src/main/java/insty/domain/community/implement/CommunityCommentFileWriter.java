package insty.domain.community.implement;

import insty.domain.common.FileCreateReq;
import insty.domain.common.FileInfo;
import insty.domain.community.repository.CommunityCommentFileRepository;
import insty.domain.file.implement.FileWriter;
import insty.global.property.AppProperties;
import insty.model.community.CommunityComment;
import insty.model.community.CommunityCommentFile;
import insty.model.file.File;
import insty.model.file.FileContainerType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
@Transactional
public class CommunityCommentFileWriter {

    private final FileWriter fileWriter;
    private final CommunityCommentFileRepository communityCommentFileRepository;
    private final AppProperties appProperties;

    public List<FileInfo> saveCommentFiles(CommunityComment comment, List<MultipartFile> addFiles) {
        saveFiles(comment, addFiles);
        return communityCommentFileRepository.findAllByCommunityComment_Id(comment.getId()).stream()
                .map(cf -> FileInfo.from(cf.getFile(), appProperties.getDomain()))
                .toList();
    }

    public List<FileInfo> updateCommentFiles(CommunityComment comment, List<MultipartFile> addFiles, List<Long> deleteFileIds) {
        if (deleteFileIds != null && !deleteFileIds.isEmpty()) {
            communityCommentFileRepository.deleteByCommentIdAndFileIds(comment.getId(), deleteFileIds);
        }
        saveFiles(comment, addFiles);
        return communityCommentFileRepository.findAllByCommunityComment_Id(comment.getId()).stream()
                .map(cf -> FileInfo.from(cf.getFile(), appProperties.getDomain()))
                .toList();
    }

    private List<CommunityCommentFile> saveFiles(CommunityComment comment, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) return List.of();
        List<FileCreateReq> fileCreateReqs = files.stream()
                .filter(file -> file != null && !file.isEmpty())
                .map(file -> new FileCreateReq(file, FileContainerType.COMMUNITY_COMMENT_IMAGE, comment.getId()))
                .toList();
        List<File> savedFiles = fileWriter.saveFiles(fileCreateReqs);
        List<CommunityCommentFile> communityCommentFiles = savedFiles.stream()
                .map(file -> CommunityCommentFile.create(comment, file))
                .toList();
        communityCommentFileRepository.saveAll(communityCommentFiles);
        return communityCommentFiles;
    }

    public void deleteCommentFiles(CommunityComment comment) {
        comment.removeAllFiles();
        communityCommentFileRepository.deleteAllByCommentId(comment.getId());
        fileWriter.deleteAllFile(FileContainerType.COMMUNITY_COMMENT_IMAGE, comment.getId());
    }
}
