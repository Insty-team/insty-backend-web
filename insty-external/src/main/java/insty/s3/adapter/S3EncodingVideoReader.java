package insty.s3.adapter;

import insty.exception.CustomException;
import insty.s3.error.S3ErrorCode;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@Slf4j
@Service
public class S3EncodingVideoReader {

    private final S3Client s3Client;
    private final String bucket;

    public S3EncodingVideoReader(
            S3Client s3Client,
            @Value("${aws.s3-encoding-video-bucket}")
            String bucket
    ) {
        this.s3Client = s3Client;
        this.bucket = bucket;
    }

    public String loadM3u8Content(String m3u8Key) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(m3u8Key)
                .build();

        try (ResponseInputStream<GetObjectResponse> s3Stream = s3Client.getObject(request);
             BufferedReader reader = new BufferedReader(new InputStreamReader(s3Stream))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            log.error("S3 에러\n", e);
            throw new CustomException(S3ErrorCode.S3_FETCH_FILE_ERROR);
        }
    }
}
