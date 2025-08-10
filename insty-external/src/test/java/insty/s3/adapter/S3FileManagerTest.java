package insty.s3.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import insty.uuid.UuidProvider;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class S3FileManagerTest {

    @InjectMocks
    private S3FileManager s3FileManager;

    @Mock
    private S3Client s3Client;
    @Mock
    private UuidProvider uuidProvider;

    @BeforeEach
    void setUp() {
        s3FileManager = new S3FileManager(s3Client, "test-bucket", uuidProvider);
    }

    @Test
    void upload_정상() throws IOException {
        // given
        String directory = "COURSE_THUMBNAIL";
        String key = "1";

        // mock
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename())
                .thenReturn("image.jpg");
        when(file.getContentType())
                .thenReturn("image/jpeg");
        byte[] fileContent = "fake-content".getBytes();
        when(file.getInputStream())
                .thenReturn(new ByteArrayInputStream(fileContent));
        when(file.getSize())
                .thenReturn((long) fileContent.length);
        when(uuidProvider.generate())
                .thenReturn(UUID.fromString("00000000-0000-0000-0000-000000000001"));

        // when
        String uploadName = s3FileManager.upload(file, directory, key);

        // then
        assertThat(uploadName).isEqualTo("00000000-0000-0000-0000-000000000001.jpg");
    }

    @Test
    void delete_정상() {
        // given
        String directory = "COURSE_THUMBNAIL";
        String key = "1";
        String fileName = "00000000-0000-0000-0000-000000000001.jpg";

        // when

        // then
        assertThatCode(() -> s3FileManager.delete(directory, key, fileName))
                .doesNotThrowAnyException();
    }

    @Test
    void deleteAllByDirectory_정상() {
        // given
        String directory = "vod/COURSE/hls/00000000-0000-0000-0000-000000000001";

        // mock
        String prefix = directory + "/";
        ListObjectsV2Response listRes = ListObjectsV2Response.builder()
                .contents(
                        S3Object.builder().key(prefix + "fileName.mp4.m3u8").size(123L).build(),
                        S3Object.builder().key(prefix + "fileName.mp4_1080p_00001.ts").size(456L).build()
                )
                .build();
        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class)))
                .thenReturn(listRes);

        // when

        // then
        assertThatCode(() -> s3FileManager.deleteAllByDirectory(directory))
                .doesNotThrowAnyException();

        // s3 파일 2개 + 디렉토리(prefix) 1개 = 3개
        ArgumentCaptor<DeleteObjectRequest> captor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(s3Client, times(3)).deleteObject(captor.capture());
    }

    @Test
    void doesFileExist_정상() {
        // given
        String key = "file/VIDEO_BASIC_THUMBNAIL/00000000-0000-0000-0000-000000000001/basic_thumbnail.jpg";

        // when
        boolean result = s3FileManager.doesFileExist(key);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void doesFileExist_정상_썸네일을_찾지_못함() {
        // given
        String key = "file/VIDEO_BASIC_THUMBNAIL/00000000-0000-0000-0000-000000000001/basic_thumbnail.jpg";

        // mock
        S3Exception accessDenied403 = (S3Exception) S3Exception.builder()
                .statusCode(403)
                .build();

        when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenThrow(accessDenied403);

        // when
        boolean result = s3FileManager.doesFileExist(key);

        // then
        assertThat(result).isFalse();
    }
}