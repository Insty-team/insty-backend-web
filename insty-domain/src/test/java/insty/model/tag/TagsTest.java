package insty.model.tag;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class TagsTest {

    @Test
    void create_정상() {
        // given
        String tagName = "태그 이름";

        // when
        Tags tags = Tags.create(tagName);

        // then
        assertThat(tags).isNotNull();
        assertThat(tags.getTagName()).isEqualTo(tagName);
    }
}