package insty.s3.adapter;

import insty.exception.CustomException;
import insty.s3.error.S3ErrorCode;
import java.io.IOException;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class S3FileManager {

    private final S3Client s3Client;
    private final String bucket;

    public S3FileManager(
            S3Client s3Client,
            @Value("${aws.s3.file.bucket}") String bucket
    ) {
        this.s3Client = s3Client;
        this.bucket = bucket;
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
        String fileName = UUID.randomUUID() + "." + StringUtils.getFilenameExtension(file.getOriginalFilename());
        String path = "file" + directory + "/" + key + "/" + fileName;

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(path)
                .contentType(file.getContentType())
                .build();

        try {
            s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (IOException e) {
            throw new CustomException(S3ErrorCode.S3_UPLOAD_ERROR);
        }
        return fileName;
    }

    public void delete(String key) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        s3Client.deleteObject(request);
    }
}
