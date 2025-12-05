package insty.domain.auth.implement.passwordreset;

import insty.domain.auth.util.StringObjectMapper;
import insty.model.auth.PasswordResetVerification;
import insty.redis.adapter.RedisService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetWriterTest {

    @Mock
    RedisService redisService;

    @InjectMocks
    PasswordResetWriter passwordResetWriter;

    @Test
    void save시_redis_호출_확인(){
        //given
        PasswordResetVerification verification = PasswordResetVerification.create("test@insty.com", length -> "abcdef");
        String expectedJson = StringObjectMapper.toJson(verification);
        //when
        passwordResetWriter.save(verification);
        //then
        verify(redisService).save(
                eq("pw-reset:test@insty.com"),
                eq(expectedJson),
                any(Duration.class)
        );
    }

}