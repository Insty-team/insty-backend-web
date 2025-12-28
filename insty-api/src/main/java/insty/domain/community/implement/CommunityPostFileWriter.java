package insty.domain.community.implement;

import insty.domain.common.FileCreateReq;
import insty.domain.common.FileInfo;
import insty.domain.community.repository.CommunityPostFileRepository;
import insty.domain.file.implement.FileWriter;
import insty.global.property.AppProperties;
import insty.model.community.CommunityPost;
import insty.model.community.CommunityPostFile;
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
public class CommunityPostFileWriter {

    private final FileWriter fileWriter;
    private final CommunityPostFileRepository communityPostFileRepository;
    private final AppProperties appProperties;

    public List<FileInfo> savePostFiles(CommunityPost post, List<MultipartFile> addFiles) {
        saveFiles(post, addFiles);
        return communityPostFileRepository.findAllByCommunityPost_Id(post.getId()).stream()
                .map(cf -> FileInfo.from(cf.getFile(), appProperties.getDomain()))
                .toList();
    }

    public List<FileInfo> updatePostFiles(CommunityPost post, List<MultipartFile> addFiles, List<Long> deleteFileIds) {
        if (deleteFileIds != null && !deleteFileIds.isEmpty()) {
            communityPostFileRepository.deleteByPostIdAndFileIds(post.getId(), deleteFileIds);
        }
        saveFiles(post, addFiles);
        return communityPostFileRepository.findAllByCommunityPost_Id(post.getId()).stream()
                .map(cf -> FileInfo.from(cf.getFile(), appProperties.getDomain()))
                .toList();
    }

    private List<CommunityPostFile> saveFiles(CommunityPost post, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) return List.of();
        List<FileCreateReq> fileCreateReqs = files.stream()
                .filter(file -> file != null && !file.isEmpty())
                .map(file -> new FileCreateReq(file, FileContainerType.COMMUNITY_POST_IMAGE, post.getId()))
                .toList();
        List<File> savedFiles = fileWriter.saveFiles(fileCreateReqs);
        List<CommunityPostFile> communityPostFiles = savedFiles.stream()
                .map(file -> CommunityPostFile.create(post, file))
                .toList();
        communityPostFileRepository.saveAll(communityPostFiles);
        return communityPostFiles;
    }

    public void deletePostFiles(CommunityPost post) {
        post.removeAllFiles();
        communityPostFileRepository.deleteAllByPostId(post.getId());
        fileWriter.deleteAllFile(FileContainerType.COMMUNITY_POST_IMAGE, post.getId());
    }
}
