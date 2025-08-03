package insty.domain.community.implement;

import insty.domain.common.FileInfo;
import insty.domain.community.repository.CommunityQuestionFileRepository;
import insty.global.property.AppProperties;
import insty.model.community.CommunityQuestion;
import insty.model.community.CommunityQuestionFile;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommunityQuestionFileReader {

    private final AppProperties appProperties;
    private final CommunityQuestionFileRepository communityQuestionFileRepository;

    /**
     * 질문의 첨부파일 정보를 FileInfo로 반환
     */
    public List<FileInfo> getQuestionFileInfos(CommunityQuestion question) {
        List<CommunityQuestionFile> files = question.getAttachments();
        if (files == null || files.isEmpty()) {
            return List.of();
        }
        return files.stream()
                .map(file -> FileInfo.from(file.getFile(), appProperties.getDomain()))
                .toList();
    }

    /**
     * 질문의 현재 파일 개수 반환
     */
    public int getCurrentFileCount(Long questionId) {
        return communityQuestionFileRepository.countByCommunityQuestionId(questionId);
    }
}