package insty.model.tag;

public class TagsFixture {

    public static Tags getTags() {
        return Tags.create("태그1");
    }

    public static Tags getTags(String name) {
        return Tags.create(name);
    }
}
