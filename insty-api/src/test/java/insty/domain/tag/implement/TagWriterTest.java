package insty.domain.tag.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import insty.domain.tag.repository.TagsRepository;
import insty.model.tag.Tags;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class TagWriterTest {

    @InjectMocks
    private TagWriter tagWriter;

    @Mock
    private TagsRepository tagsRepository;

    @Test
    void saveTags_정상() {
        // given
        String tag1 = "이미 저장되어 있던 태그";
        String tag2 = "새롭게 추가되는 태그";
        Set<String> tagNames = Set.of(tag1, tag2);

        // mock
        when(tagsRepository.findByTagNameIn(tagNames))
                .thenReturn(new HashSet<>(List.of(Tags.create(tag1))));
        when(tagsRepository.saveAll(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Set<Tags> result = tagWriter.saveTags(tagNames);

        // then
        assertThat(result.size()).isEqualTo(2);
        Set<String> set = result.stream()
                .map(Tags::getTagName)
                .collect(Collectors.toSet());
        assertThat(set).containsExactlyInAnyOrder(tag1, tag2);
    }
}