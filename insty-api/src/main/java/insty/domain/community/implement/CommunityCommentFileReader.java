package insty.domain.community.implement;

import insty.domain.common.FileInfo;
import insty.domain.community.repository.CommunityCommentFileRepository;
import insty.global.property.AppProperties;
import insty.model.community.CommunityComment;
import insty.model.community.CommunityCommentFile;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommunityCommentFileReader {

    private final AppProperties appProperties;
    private final CommunityCommentFileRepository communityCommentFileRepository;

    public List<FileInfo> getCommentFileInfos(CommunityComment comment) {
        List<CommunityCommentFile> files = comment.getAttachments();
        if (files == null || files.isEmpty()) {
            return List.of();
        }
        String domain = appProperties.getDomain();
        return files.stream()
                .map(file -> FileInfo.from(file.getFile(), domain))
                .toList();
    }

    public int getCurrentFileCount(Long commentId) {
        return communityCommentFileRepository.countByCommunityComment_Id(commentId);
    }
}
