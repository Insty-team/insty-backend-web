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

    public static File getFileWithId(Long fileId, FileContainerType containerType, Long containerId, String name,
                                     String originalName, String contentType, long size) {
        File file = FileFixture.getFile(containerType, containerId, name, originalName, contentType, size);
        ReflectionTestUtils.setField(file, "id", fileId);
        return file;
    }
}
