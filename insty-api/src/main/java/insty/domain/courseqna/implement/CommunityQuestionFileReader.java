package insty.domain.courseqna.implement;

import insty.domain.common.FileInfo;
import insty.domain.courseqna.repository.CourseQuestionFileRepository;
import insty.global.property.AppProperties;
import insty.model.courseqna.CourseQuestion;
import insty.model.courseqna.CourseQuestionFile;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommunityQuestionFileReader {

    private final AppProperties appProperties;
    private final CourseQuestionFileRepository courseQuestionFileRepository;

    /**
     * 질문의 첨부파일 정보를 FileInfo로 반환
     */
    public List<FileInfo> getQuestionFileInfos(CourseQuestion question) {
        List<CourseQuestionFile> files = question.getAttachments();
        if (files == null || files.isEmpty()) {
            return List.of();
        }
        String domain = appProperties.getDomain();
        return files.stream()
                .map(file -> FileInfo.from(file.getFile(), domain))
                .toList();
    }

    /**
     * 질문의 현재 파일 개수 반환
     */
    public int getCurrentFileCount(Long questionId) {
        return courseQuestionFileRepository.countByCourseQuestionId(questionId);
    }
}