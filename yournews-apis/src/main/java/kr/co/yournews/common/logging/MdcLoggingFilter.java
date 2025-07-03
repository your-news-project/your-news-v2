package kr.co.yournews.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class MdcLoggingFilter extends OncePerRequestFilter {

    private static final String TRACE_ID = "traceId";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            // 요청마다 traceId 생성
            String traceId = UUID.randomUUID().toString();
            MDC.put(TRACE_ID, traceId);

            filterChain.doFilter(request, response);
        } finally {
            // 요청 끝나면 MDC 비우기
            MDC.remove(TRACE_ID);
        }
    }
}
