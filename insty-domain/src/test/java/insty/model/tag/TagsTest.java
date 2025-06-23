package insty.model.tag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import insty.error.TagErrorCode;
import insty.exception.CustomException;
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
        assertThat(tags.getId()).isNull();
        assertThat(tags.getTagName()).isEqualTo(tagName);
    }

    @Test
    void create_에러_tagName이_null이다() {
        // given
        String tagName = null;

        // when

        // then
        assertThatThrownBy(() -> Tags.create(tagName))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(TagErrorCode.TAG_CREATE_ERROR);
    }

    @Test
    void create_에러_tagName이_비었다() {
        // given
        String tagName = "  \n\t\r";

        // when

        // then
        assertThatThrownBy(() -> Tags.create(tagName))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(TagErrorCode.TAG_CREATE_ERROR);
    }
}