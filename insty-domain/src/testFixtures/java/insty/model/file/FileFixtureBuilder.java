package insty.model.file;

import org.springframework.test.util.ReflectionTestUtils;

public class FileFixtureBuilder {

    public static File getCourseThumbnailWithId() {
        File courseThumbnail = FileFixture.getCourseThumbnail();
        ReflectionTestUtils.setField(courseThumbnail, "id", 1L);
        return courseThumbnail;
    }

    public static File getCoursePracticeFileWithId() {
        File coursePracticeFile = FileFixture.getCoursePracticeFile();
        ReflectionTestUtils.setField(coursePracticeFile, "id", 1L);
        return coursePracticeFile;
    }
}
