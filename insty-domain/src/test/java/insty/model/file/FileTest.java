package insty.model.file;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import insty.error.FileErrorCode;
import insty.exception.CustomException;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class FileTest {

    @Test
    void create_정상() {
        // given
        FileContainerType containerType = FileContainerType.COURSE_THUMBNAIL;
        Long containerId = 1L;
        String name = "00000000-0000-0000-0000-000000000001.jpg";
        String originalName = "fileName.jpg";
        String contentType = "image/jpeg";
        long size = 1024L;

        // when
        File file = File.create(containerType, containerId, name, originalName, contentType, size);

        // then
        assertThat(file).isNotNull();
        assertThat(file.getContainerType()).isEqualTo(containerType);
        assertThat(file.getContainerId()).isEqualTo(containerId);
        assertThat(file.getName()).isEqualTo(name);
        assertThat(file.getOriginalName()).isEqualTo(originalName);
        assertThat(file.getContentType()).isEqualTo(contentType);
        assertThat(file.getSize()).isEqualTo(size);
    }

    @Test
    void create_에러_containerType이_null이다() {
        // given
        FileContainerType containerType = null;
        Long containerId = 1L;
        String name = UUID.fromString("00000000-0000-0000-0000-000000000001").toString() + ".jpg";
        String originalName = "fileName.jpg";
        String contentType = "image/jpeg";
        long size = 10L;

        // when

        // then
        assertThatThrownBy(() -> File.create(containerType, containerId, name, originalName, contentType, size))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(FileErrorCode.FILE_CREATE_ERROR);
    }

    @Test
    void create_에러_containerId가_null이다() {
        // given
        FileContainerType containerType = FileContainerType.COURSE_THUMBNAIL;
        Long containerId = null;
        String name = UUID.fromString("00000000-0000-0000-0000-000000000001").toString() + ".jpg";
        String originalName = "fileName.jpg";
        String contentType = "image/jpeg";
        long size = 10L;

        // when

        // then
        assertThatThrownBy(() -> File.create(containerType, containerId, name, originalName, contentType, size))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(FileErrorCode.FILE_CREATE_ERROR);
    }

    @Test
    void create_에러_name이_null이다() {
        // given
        FileContainerType containerType = FileContainerType.COURSE_THUMBNAIL;
        Long containerId = 1L;
        String name = null;
        String originalName = "fileName.jpg";
        String contentType = "image/jpeg";
        long size = 10L;

        // when

        // then
        assertThatThrownBy(() -> File.create(containerType, containerId, name, originalName, contentType, size))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(FileErrorCode.FILE_CREATE_ERROR);
    }

    @Test
    void create_에러_name이_비었다() {
        // given
        FileContainerType containerType = FileContainerType.COURSE_THUMBNAIL;
        Long containerId = 1L;
        String name = "  \n\t\r";
        String originalName = "fileName.jpg";
        String contentType = "image/jpeg";
        long size = 10L;

        // when

        // then
        assertThatThrownBy(() -> File.create(containerType, containerId, name, originalName, contentType, size))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(FileErrorCode.FILE_CREATE_ERROR);
    }

    @Test
    void create_에러_originalName이_null이다() {
        // given
        FileContainerType containerType = FileContainerType.COURSE_THUMBNAIL;
        Long containerId = 1L;
        String name = UUID.fromString("00000000-0000-0000-0000-000000000001").toString() + ".jpg";
        String originalName = null;
        String contentType = "image/jpeg";
        long size = 10L;

        // when

        // then
        assertThatThrownBy(() -> File.create(containerType, containerId, name, originalName, contentType, size))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(FileErrorCode.FILE_CREATE_ERROR);
    }

    @Test
    void create_에러_originalName이_비었다() {
        // given
        FileContainerType containerType = FileContainerType.COURSE_THUMBNAIL;
        Long containerId = 1L;
        String name = UUID.fromString("00000000-0000-0000-0000-000000000001").toString() + ".jpg";
        String originalName = "  \n\t\r";
        String contentType = "image/jpeg";
        long size = 10L;

        // when

        // then
        assertThatThrownBy(() -> File.create(containerType, containerId, name, originalName, contentType, size))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(FileErrorCode.FILE_CREATE_ERROR);
    }

    @Test
    void create_에러_contentType이_null이다() {
        // given
        FileContainerType containerType = FileContainerType.COURSE_THUMBNAIL;
        Long containerId = 1L;
        String name = UUID.fromString("00000000-0000-0000-0000-000000000001").toString() + ".jpg";
        String originalName = "fileName.jpg";
        String contentType = null;
        long size = 10L;

        // when

        // then
        assertThatThrownBy(() -> File.create(containerType, containerId, name, originalName, contentType, size))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(FileErrorCode.FILE_CREATE_ERROR);
    }

    @Test
    void create_에러_contentType이_비었다() {
        // given
        FileContainerType containerType = FileContainerType.COURSE_THUMBNAIL;
        Long containerId = 1L;
        String name = UUID.fromString("00000000-0000-0000-0000-000000000001").toString() + ".jpg";
        String originalName = "fileName.jpg";
        String contentType = "  \n\t\r";
        long size = 10L;

        // when

        // then
        assertThatThrownBy(() -> File.create(containerType, containerId, name, originalName, contentType, size))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(FileErrorCode.FILE_CREATE_ERROR);
    }

    @Test
    void create_에러_size가_0_미만이다() {
        // given
        FileContainerType containerType = FileContainerType.COURSE_THUMBNAIL;
        Long containerId = 1L;
        String name = UUID.fromString("00000000-0000-0000-0000-000000000001").toString() + ".jpg";
        String originalName = "fileName.jpg";
        String contentType = "image/jpeg";
        long size = -1L;

        // when

        // then
        assertThatThrownBy(() -> File.create(containerType, containerId, name, originalName, contentType, size))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(FileErrorCode.FILE_CREATE_ERROR);
    }

    @Test
    void getUrl_정상() {
        // given
        String domain = "insty.test.com";
        File file = File.create(FileContainerType.COURSE_THUMBNAIL, 1L, "00000000-0000-0000-0000-000000000001.jpg",
                "thumb.jpg", "image/jpeg", 10);

        // when
        String url = file.getUrl(domain);

        // then
        assertThat(url).isNotNull();
        assertThat(url).isEqualTo(
                "https://insty.test.com/file/COURSE_THUMBNAIL/1/00000000-0000-0000-0000-000000000001.jpg");
    }
}