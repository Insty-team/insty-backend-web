package insty.s3.adapter;

import insty.exception.CustomException;
import insty.s3.error.S3ErrorCode;
import insty.uuid.UuidProvider;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Slf4j
@Service
public class S3FileManager {

    private final S3Client s3Client;
    private final String bucket;
    private final UuidProvider uuidProvider;

    public S3FileManager(
            S3Client s3Client,
            @Value("${aws.s3.file.bucket}") String bucket,
            UuidProvider uuidProvider
    ) {
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.uuidProvider = uuidProvider;
    }

    /**
     * MultipartFile로 s3에 업로드한다.<br> path 예시 = file/COURSE_THUMBNAIL/1/uuid.ext
     *
     * @param file
     * @param directory {FileContainerType} (COURSE_THUMBNAIL)
     * @param key       {ContainerId} (1)
     * @return {fileName} (uuid.png)
     */
    public String upload(MultipartFile file, String directory, String key) {
        String fileName = uuidProvider.generate() + "." + StringUtils.getFilenameExtension(file.getOriginalFilename());

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(getFilePath(directory, key, fileName))
                .contentType(file.getContentType())
                .build();

        try {
            s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new CustomException(S3ErrorCode.S3_UPLOAD_ERROR);
        }
        return fileName;
    }

    public void delete(String directory, String key, String fileName) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(getFilePath(directory, key, fileName))
                .key(key)
                .build();

        s3Client.deleteObject(request);
    }

    private String getFilePath(String directory, String key, String fileName) {
        return "file/" + directory + "/" + key + "/" + fileName;
    }

    public boolean doesFileExist(String key) {
        HeadObjectRequest request = HeadObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        try {
            s3Client.headObject(request);
            return true;
        } catch (S3Exception e) {
            if (e.statusCode() == 403) {
                return false;
            }
            log.error(e.getMessage(), e);
            throw new CustomException(S3ErrorCode.S3_HEAD_ERROR);
        }
    }
}
