package insty.model.file;

public class FileFixture {

    public static File getCourseThumbnail() {
        return File.create(FileContainerType.COURSE_THUMBNAIL, 1L, "00000000-0000-0000-0000-000000000001.jpg",
                "thumbnail.jpg", "image/jpeg", 10);
    }

    public static File getCoursePracticeFile() {
        return File.create(FileContainerType.COURSE_PRACTICE_FILE, 1L, "00000000-0000-0000-0000-000000000001.jpg",
                "practice.jpg", "image/jpeg", 10);
    }

    public static File getFile(FileContainerType containerType, Long containerId, String name, String originalName,
                               String contentType, long size) {
        return File.create(containerType, containerId, name, originalName, contentType, size);
    }
}
