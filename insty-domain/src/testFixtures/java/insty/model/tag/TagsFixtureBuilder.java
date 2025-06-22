package insty.model.tag;

import org.springframework.test.util.ReflectionTestUtils;

public class TagsFixtureBuilder {

    public static Tags getTagsWithId() {
        Tags tags = TagsFixture.getTags();
        ReflectionTestUtils.setField(tags, "id", 1L);
        return tags;
    }
}
