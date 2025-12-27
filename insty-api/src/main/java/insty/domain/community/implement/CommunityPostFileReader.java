package insty.domain.community.implement;

import insty.domain.common.FileInfo;
import insty.domain.community.repository.CommunityPostFileRepository;
import insty.global.property.AppProperties;
import insty.model.community.CommunityPost;
import insty.model.community.CommunityPostFile;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommunityPostFileReader {

    private final AppProperties appProperties;
    private final CommunityPostFileRepository communityPostFileRepository;

    public List<FileInfo> getPostFileInfos(CommunityPost post) {
        List<CommunityPostFile> files = post.getAttachments();
        if (files == null || files.isEmpty()) {
            return List.of();
        }
        String domain = appProperties.getDomain();
        return files.stream()
                .map(file -> FileInfo.from(file.getFile(), domain))
                .toList();
    }

    public int getCurrentFileCount(Long postId) {
        return communityPostFileRepository.countByCommunityPost_Id(postId);
    }
}
