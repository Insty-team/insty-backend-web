package insty.s3.adapter;

import insty.s3.constant.S3Constants;
import insty.s3.dto.PresignedUrlDto;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
public class S3UrlIssuer {

    private final S3Presigner s3Presigner;
    private final String bucket;

    public S3UrlIssuer(
            S3Presigner s3Presigner,
            @Value("${aws.s3-upload-bucket}") String bucket
    ) {
        this.s3Presigner = s3Presigner;
        this.bucket = bucket;
    }

    /**
     * 사용자가 영상을 업로드 할 수 있는 Pre-Signed URL을 발급한다.
     *
     * @param key         vod/COURSE/mp4/uuid/fileName.mp4
     * @param contentType video/mp4
     * @return url, 만료일자
     */
    public PresignedUrlDto generatePresignedUrl(String key, String contentType) {
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .putObjectRequest(objectRequest)
                .signatureDuration(Duration.ofMinutes(S3Constants.UPLOAD_URL_EXPIRATION_MINUTES))
                .build();

        PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(presignRequest);
        return new PresignedUrlDto(presigned.url().toString(), presigned.expiration());
    }
}
