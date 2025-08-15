package insty.domain.auth.util;

import static insty.error.TokenErrorCode.ACCESS_TOKEN_MISSING;

import insty.global.security.exception.CustomAuthenticationException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class TokenExtractor {
    
    public String getCurrentToken() {
        HttpServletRequest request =
            ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        
        throw new CustomAuthenticationException(ACCESS_TOKEN_MISSING);
    }
}