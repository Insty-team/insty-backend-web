package insty.domain.course.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import insty.redis.adapter.RedisService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class CourseViewCountLimiterTest {

    @InjectMocks
    private CourseViewCountLimiter courseViewCountLimiter;

    @Mock
    private RedisService redisService;

    @Test
    void allowIncrease_처음_조회면_true() {
        // given
        when(redisService.saveIfAbsent(anyString(), anyString(), any()))
                .thenReturn(true);

        // when
        boolean result = courseViewCountLimiter.allowIncrease(1L, 2L);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void allowIncrease_중복_조회면_false() {
        // given
        when(redisService.saveIfAbsent(anyString(), anyString(), any()))
                .thenReturn(false);

        // when
        boolean result = courseViewCountLimiter.allowIncrease(1L, 2L);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void allowIncrease_Redis_오류시_true() {
        // given
        doThrow(new RuntimeException("redis down"))
                .when(redisService)
                .saveIfAbsent(anyString(), anyString(), any());

        // when
        boolean result = courseViewCountLimiter.allowIncrease(1L, 2L);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void allowIncrease_userId_null이면_true() {
        // when
        boolean result = courseViewCountLimiter.allowIncrease(1L, null);

        // then
        assertThat(result).isTrue();
    }
}
