package insty.model.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SimpleTokenGeneratorTest {

    private TokenGenerator tokenGenerator = new SimpleTokenGenerator();

    @Test
    void 자리수로_유효한_토큰이_생성된다() {
        // given
        int length = 6;

        // when
        String token = tokenGenerator.generate(length);

        // then
        assertThat(token).hasSize(length);
    }

    @Test
    void 토큰은_숫자로만_구성된다() {
        // given
        int length = 10;

        // when
        String token = tokenGenerator.generate(length);

        // then
        assertThat(token.matches("\\d{" + length + "}")).isTrue();
    }
}