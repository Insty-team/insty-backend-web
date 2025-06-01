package insty.global.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import insty.domain.user.dto.response.UserLoginRes;
import insty.global.response.SuccessRes;
import insty.model.user.UserType;
import insty.util.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class LoginSuccessHandlerTest {

    @Mock
    private JwtUtils jwtHelper;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private LoginSuccessHandler successHandler;

    @Test
    void 로그인_성공시_JSON_응답_작성() throws Exception {
        // given
        CustomUserDetails user = mock(CustomUserDetails.class);
        when(user.getUserId()).thenReturn(1L);
        when(user.getUsername()).thenReturn("tester");
        when(user.getUserType()).thenReturn(UserType.NONE);

        String accessToken = "access-token";
        String refreshToken = "refresh-token";
        when(jwtHelper.generateAccessToken("1", UserType.NONE.name())).thenReturn(accessToken);
        when(jwtHelper.generateRefreshToken("1")).thenReturn(refreshToken);
        when(jwtHelper.extractExpiredAt(accessToken)).thenReturn(123456789L);
        when(jwtHelper.extractExpiredAt(refreshToken)).thenReturn(987654321L);

        UserLoginRes userLoginRes = UserLoginRes.create(1L, "tester", UserType.NONE, accessToken, refreshToken, 123456789L, 987654321L);
        SuccessRes<UserLoginRes> successRes = SuccessRes.of(userLoginRes);
        String jsonResponse = objectMapper.writeValueAsString(successRes);

        when(objectMapper.writeValueAsString(any(SuccessRes.class))).thenReturn(jsonResponse);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        PrintWriter writer = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(writer);

        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

        // when
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // then
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(response).setCharacterEncoding(StandardCharsets.UTF_8.toString());
        verify(response).setStatus(HttpServletResponse.SC_OK);
        verify(writer).write(jsonResponse);
    }
}
