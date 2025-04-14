package kr.co.yournews.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.yournews.auth.filter.JwtAuthenticationFilter;
import kr.co.yournews.auth.filter.JwtExceptionFilter;
import kr.co.yournews.auth.jwt.provider.JwtProvider;
import kr.co.yournews.auth.service.TokenBlackListService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
@RequiredArgsConstructor
public class SecurityFilterConfig {
    private final UserDetailsService userDetailsService;
    private final JwtProvider jwtProvider;
    private final TokenBlackListService tokenBlackListService;
    private final ObjectMapper objectMapper;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(userDetailsService, jwtProvider, tokenBlackListService);
    }

    @Bean
    public JwtExceptionFilter jwtExceptionFilter() {
        return new JwtExceptionFilter(objectMapper);
    }
}
