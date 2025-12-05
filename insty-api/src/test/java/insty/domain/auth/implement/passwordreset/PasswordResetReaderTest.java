package insty.domain.auth.implement.passwordreset;

import insty.domain.auth.util.StringObjectMapper;
import insty.error.AuthErrorCode;
import insty.exception.CustomException;
import insty.model.auth.PasswordResetVerification;
import insty.redis.adapter.RedisService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetReaderTest {

    @Mock
    RedisService redisService;

    @InjectMocks
    PasswordResetReader reader;

    @Test
    void findOptionalByEmail_미인증상태_정상() {

        // given
        String json = "{\"email\":\"test@example.com\",\"verified\":true}";

        //mock
        when(redisService.find("pw-reset:test@example.com")).thenReturn(Optional.of(json));

        // when
        Optional<PasswordResetVerification> result =
                reader.findOptionalByEmail("test@example.com");

        // then
        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
        assertTrue(result.get().isVerified());
    }

    @Test
    void findByEmail_이메일_내역이_redis에_없는경우() {
        //given
        // mock
        when(redisService.find("pw-reset:test@example.com")).thenReturn(Optional.empty());

        // when & then
        CustomException e = assertThrows(CustomException.class, () ->
                reader.findByEmail("test@example.com")
        );

        assertEquals(AuthErrorCode.REQUIRES_EMAIL_VERIFICATION_REQUEST, e.getErrorCode());
    }

    @Test
    void isVerified_정상() {

        //given
        String json = "{\"email\":\"test@example.com\",\"verified\":true}";

        //mock
        when(redisService.find("pw-reset:test@example.com")).thenReturn(Optional.of(json));

        //when&then
        assertTrue(reader.isVerified("test@example.com"));
    }

    @Test
    void isVerified_redis에_등록_되지않아_인증이_실패하여_false일때() {
        //given
        //mock
        when(redisService.find("pw-reset:test@example.com")).thenReturn(Optional.empty());

        //when&then
        assertFalse(reader.isVerified("test@example.com"));
    }

    @Test
    void isVerified_redis에_등록은_되어있으나_인증상태가_false일때() {
        //given
        String json = "{\"email\":\"test@example.com\",\"verified\":false}";
        //mock
        when(redisService.find("pw-reset:test@example.com")).thenReturn(Optional.of(json));
        //when&then
        assertFalse(reader.isVerified("test@example.com"));
    }
}