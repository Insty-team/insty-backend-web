package insty.model.tag;

import org.springframework.test.util.ReflectionTestUtils;

public class TagsFixtureBuilder {

    public static Tags getTagsWithId() {
        Tags tags = TagsFixture.getTags();
        ReflectionTestUtils.setField(tags, "id", 1L);
        return tags;
    }

    public static Tags getTagsWithId(Long tagsId, String tagName) {
        Tags tags = TagsFixture.getTags(tagName);
        ReflectionTestUtils.setField(tags, "id", tagsId);
        return tags;
    }
}
