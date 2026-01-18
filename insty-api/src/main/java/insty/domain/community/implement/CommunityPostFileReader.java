package insty.domain.community.implement;

import insty.domain.common.FileInfo;
import insty.domain.community.repository.CommunityPostFileRepository;
import insty.global.property.AppProperties;
import insty.model.community.CommunityPost;
import insty.model.community.CommunityPostFile;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
        return getPostFileInfos(List.of(post.getId())).getOrDefault(post.getId(), List.of());
    }

    public Map<Long, List<FileInfo>> getPostFileInfos(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Map.of();
        }
        List<CommunityPostFile> files = communityPostFileRepository.findAllWithFileByPostIds(postIds);
        if (files.isEmpty()) {
            return Map.of();
        }
        String domain = appProperties.getDomain();
        return files.stream()
                .collect(Collectors.groupingBy(
                        file -> file.getCommunityPost().getId(),
                        Collectors.mapping(file -> FileInfo.from(file.getFile(), domain), Collectors.toList())
                ));
    }

    public int getCurrentFileCount(Long postId) {
        return communityPostFileRepository.countByCommunityPost_Id(postId);
    }
}
