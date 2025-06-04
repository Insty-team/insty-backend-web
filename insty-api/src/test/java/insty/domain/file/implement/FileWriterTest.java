package insty.domain.file.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import insty.domain.common.FileCreateReq;
import insty.domain.file.repository.FileRepository;
import insty.model.file.File;
import insty.model.file.FileContainerType;
import insty.s3.adapter.S3FileManager;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class FileWriterTest {

    @InjectMocks
    private FileWriter fileWriter;

    @Mock
    private S3FileManager s3FileManager;
    @Mock
    private FileRepository fileRepository;

    @Test
    void saveFile_정상() {
        // given
        MultipartFile multipartFile = mock(MultipartFile.class);
        FileCreateReq req = new FileCreateReq(multipartFile, FileContainerType.COURSE_THUMBNAIL, 1L);

        // mock
        when(multipartFile.getOriginalFilename())
                .thenReturn("fileName.jpg");
        when(multipartFile.getContentType())
                .thenReturn("image/jpeg");
        when(s3FileManager.upload(any(), anyString(), anyString()))
                .thenReturn("00000000-0000-0000-0000-000000000001.jpg");
        when(fileRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        File file = fileWriter.saveFile(req);

        // then
        assertThat(file).isNotNull();
//        assertThat(file.getId()).isNotNull(); // id 자동 생성 검증 생략
        assertThat(file.getContainerType()).isEqualTo(FileContainerType.COURSE_THUMBNAIL);
        assertThat(file.getContainerId()).isEqualTo(1L);
        assertThat(file.getName()).isEqualTo("00000000-0000-0000-0000-000000000001.jpg");
        assertThat(file.getOriginalName()).isEqualTo("fileName.jpg");
        assertThat(file.getContentType()).isEqualTo("image/jpeg");
    }

    @Test
    void saveFiles_정상() {
        // given
        MultipartFile multipartFile = mock(MultipartFile.class);
        FileCreateReq req1 = new FileCreateReq(multipartFile, FileContainerType.COURSE_THUMBNAIL, 1L);
        FileCreateReq req2 = new FileCreateReq(multipartFile, FileContainerType.ANSWER_THUMBNAIL, 2L);
        List<FileCreateReq> req = List.of(req1, req2);

        // mock
        when(multipartFile.getOriginalFilename())
                .thenReturn("fileName.jpg");
        when(multipartFile.getContentType())
                .thenReturn("image/jpeg");
        when(s3FileManager.upload(any(), anyString(), anyString()))
                .thenReturn("00000000-0000-0000-0000-000000000001.jpg");
        when(fileRepository.saveAll(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        List<File> files = fileWriter.saveFiles(req);

        // then
        assertThat(files).isNotNull();
        assertThat(files.size()).isEqualTo(2);
        assertThat(files.get(0).getContainerType()).isEqualTo(FileContainerType.COURSE_THUMBNAIL);
        assertThat(files.get(0).getContainerId()).isEqualTo(1L);
        assertThat(files.get(1).getContainerType()).isEqualTo(FileContainerType.ANSWER_THUMBNAIL);
        assertThat(files.get(1).getContainerId()).isEqualTo(2L);
    }

    @Test
    void deleteAllFile_정상() {
        // given
        FileContainerType containerType = FileContainerType.COURSE_THUMBNAIL;
        Long containerId = 1L;

        // mock
        when(fileRepository.findAllByContainerTypeAndContainerId(containerType, containerId))
                .thenReturn(List.of(mock(File.class)));

        // when

        // then
        assertThatCode(() -> fileWriter.deleteAllFile(containerType, containerId))
                .doesNotThrowAnyException();
    }

    @Test
    void deleteFile_정상() {
        // given
        File file = File.create(FileContainerType.COURSE_THUMBNAIL, 1L, "00000000-0000-0000-0000-000000000001.jpg",
                "thumb.jpg", "image/jpeg", 10);

        // when

        // then
        assertThatCode(() -> fileWriter.deleteFile(file))
                .doesNotThrowAnyException();
    }
}