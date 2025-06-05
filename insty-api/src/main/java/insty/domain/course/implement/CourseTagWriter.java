package insty.domain.course.implement;

import insty.domain.course.repository.CourseTagRepository;
import insty.domain.tag.implement.TagWriter;
import insty.model.course.Course;
import insty.model.course.CourseTag;
import insty.model.tag.Tags;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CourseTagWriter {

    private final TagWriter tagWriter;
    private final CourseTagRepository courseTagRepository;

    public Set<Tags> saveCourseTagsAndGetTags(Course course, Set<String> tagNames) {
        Set<Tags> tags = tagWriter.saveTags(tagNames);

        List<CourseTag> list = tags.stream()
                .map(tag -> CourseTag.create(course, tag))
                .toList();
        courseTagRepository.saveAll(list);

        return tags;
    }
}
