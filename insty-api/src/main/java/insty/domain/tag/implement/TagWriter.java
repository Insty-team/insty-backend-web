package insty.domain.tag.implement;

import insty.domain.tag.repository.TagsRepository;
import insty.model.tag.Tags;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class TagWriter {

    private final TagsRepository tagsRepository;

    /**
     * 태그 명이 유니크하게 유지되도록 저장되지 않은 태그명 새로 저장한다.<br> 이로 인해 태그 기반 필터 구현에 이점이 생긴다.
     *
     * @param tagNames 요청된 태그명 집합
     * @return 요청된 태그명에 대한 모든 태그
     */
    public List<Tags> saveTags(Set<String> tagNames) {
        List<Tags> tags = tagsRepository.findByTagNameIn(tagNames);

        List<String> alreadySavedTags = tags.stream()
                .map(Tags::getTagName)
                .toList();
        for (String tag : tagNames) {
            if (!alreadySavedTags.contains(tag)) {
                Tags newTag = tagsRepository.save(Tags.create(tag));
                tags.add(newTag);
            }
        }

        return tags;
    }
}
