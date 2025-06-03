package insty.model.file;

import static org.assertj.core.api.Assertions.assertThat;

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
}