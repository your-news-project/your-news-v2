package kr.co.yournews.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.co.yournews.auth.authentication.CustomUserDetails;
import kr.co.yournews.common.exception.AuthErrorType;
import kr.co.yournews.common.response.error.ErrorResponse;
import kr.co.yournews.common.response.error.type.BaseErrorType;
import kr.co.yournews.domain.user.type.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {
    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        boolean isGuest = userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals(Role.GUEST.getValue()));

        BaseErrorType error = isGuest
                ? AuthErrorType.UN_REGISTERED_USER
                : AuthErrorType.FORBIDDEN;

        log.warn("[AccessDenied] userId={}, requestURI={}, error={}",
                userDetails.getUserId(),
                request.getRequestURI(),
                error.getMessage()
        );

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.getWriter().write(objectMapper.writeValueAsString(ErrorResponse.from(error)));
    }
}
