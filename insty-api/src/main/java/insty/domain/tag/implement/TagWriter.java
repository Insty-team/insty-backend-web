package insty.domain.tag.implement;

import insty.domain.tag.repository.TagsRepository;
import insty.model.tag.Tags;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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
    public Set<Tags> saveTags(Set<String> tagNames) {
        Set<Tags> tags = tagsRepository.findByTagNameIn(tagNames);

        Set<String> existingTagNames = tags.stream()
                .map(Tags::getTagName)
                .collect(Collectors.toSet());
        List<Tags> newTags = tagNames.stream()
                .filter(tagName -> !existingTagNames.contains(tagName))
                .map(Tags::create)
                .toList();
        List<Tags> savedNewTags = tagsRepository.saveAll(newTags);

        tags.addAll(savedNewTags);
        return tags;
    }
}
