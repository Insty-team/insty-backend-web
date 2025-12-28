package insty.domain.courseqna.implement;

import insty.domain.common.FileInfo;
import insty.domain.courseqna.repository.CourseAnswerFileRepository;
import insty.global.property.AppProperties;
import insty.model.courseqna.CourseAnswer;
import insty.model.courseqna.CourseAnswerFile;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CourseAnswerFileReader {

    private final AppProperties appProperties;
    private final CourseAnswerFileRepository courseAnswerFileRepository;

    /**
     * 답변의 첨부파일 정보를 FileInfo로 반환
     */
    public List<FileInfo> getAnswerFileInfos(CourseAnswer answer) {
        List<CourseAnswerFile> files = answer.getAttachments();
        if (files == null || files.isEmpty()) {
            return List.of();
        }
        String domain = appProperties.getDomain();
        return files.stream()
                .map(file -> FileInfo.from(file.getFile(), domain))
                .toList();
    }

    /**
     * 답변의 현재 파일 개수 반환
     */
    public int getCurrentFileCount(Long answerId) {
        return courseAnswerFileRepository.countByCourseAnswerId(answerId);
    }
}