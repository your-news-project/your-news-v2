package kr.co.yournews.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.co.yournews.common.exception.AuthErrorType;
import kr.co.yournews.common.response.error.ErrorResponse;
import kr.co.yournews.common.response.error.type.BaseErrorType;
import kr.co.yournews.common.response.exception.BlackListException;
import kr.co.yournews.common.sentry.SentryCapture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class JwtExceptionFilter extends OncePerRequestFilter {
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            handleExceptionToken(response, AuthErrorType.ACCESS_TOKEN_EXPIRED);
        } catch (MalformedJwtException e) {
            captureJwtException(e, "malformed");
            handleExceptionToken(response, AuthErrorType.INVALID_ACCESS_TOKEN);
        } catch (SignatureException e) {
            captureJwtException(e, "invalid_signature");
            handleExceptionToken(response, AuthErrorType.INVALID_TOKEN_SIGNATURE);
        } catch (JwtException e) {
            captureJwtException(e, "jwt_unknown");
            handleExceptionToken(response, AuthErrorType.UNKNOWN_TOKEN_ERROR);
        } catch (BlackListException e) {
            captureJwtException(e, "blacklist");
            handleExceptionToken(response, AuthErrorType.BLACKLIST_ACCESS_TOKEN);
        }
    }

    /**
     * Jwt 인증 과정 중, 예외가 발생했을 때 예외를 처리하는 메서드
     */
    private void handleExceptionToken(HttpServletResponse response, BaseErrorType errorType) throws IOException {
        ErrorResponse error = ErrorResponse.from(errorType);
        String messageBody = objectMapper.writeValueAsString(error);

        log.error("[Error occurred] {}", error.message());

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(messageBody);
    }

    private void captureJwtException(Exception e, String jwtType) {
        SentryCapture.warn(
                "jwt",
                Map.of(
                        "stage", "filter",
                        "jwtType", jwtType,
                        "reason", "exception"
                ),
                Map.of(
                        "exceptionClass", e.getClass().getName()
                ),
                "[JWT] validation failed"
        );
    }
}
