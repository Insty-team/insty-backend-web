package insty.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class FileUtilsTest {

    @Test
    void extractExtension_정상() {
        // given
        String fileName = "fileName.mp4";

        // when
        Optional<String> extension = FileUtils.extractExtension(fileName);

        // then
        assertThat(extension).isPresent();
        assertThat(extension.get()).isEqualTo("mp4");
    }
    
    @Test
    void extractExtension_정상_확장자명_추출_실패() {
        // given
        String fileName = "fileName";

        // when
        Optional<String> extension = FileUtils.extractExtension(fileName);

        // then
        assertThat(extension).isEmpty();
    }
}