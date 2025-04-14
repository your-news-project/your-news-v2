package kr.co.yournews.auth.filter;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.co.yournews.auth.jwt.provider.JwtProvider;
import kr.co.yournews.auth.service.TokenBlackListService;
import kr.co.yournews.common.exception.AuthErrorType;
import kr.co.yournews.common.response.exception.BlackListException;
import kr.co.yournews.common.util.AuthConstants;
import kr.co.yournews.common.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final UserDetailsService userDetailsService;
    private final JwtProvider jwtProvider;
    private final TokenBlackListService tokenBlackListService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authToken = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authToken == null || !authToken.startsWith(AuthConstants.TOKEN_TYPE.getValue())) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = resolveAccessToken(authToken);

        Authentication auth = getAuthentication(accessToken);
        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(request, response);
    }

    /**
     * 토큰 파싱 및 유효성 검사 메서드
     *
     * @throws : 토큰 만료 및 블랙리스트 토큰이면 예외 발생
     */
    private String resolveAccessToken(String authToken) {
        String accessToken = JwtUtil.resolveToken(authToken);

        if (tokenBlackListService.existsBlackListCheck(accessToken)) {
            throw new BlackListException(AuthErrorType.BLACKLIST_ACCESS_TOKEN);
        }

        if (jwtProvider.isExpired(accessToken)) {
            throw new ExpiredJwtException(null, null, AuthErrorType.ACCESS_TOKEN_EXPIRED.getMessage());
        }

        return accessToken;
    }

    /**
     * SecurityContextHolder에 UserDetails를 등록하는 메서드
     */
    private Authentication getAuthentication(String token) {
        String userId = String.valueOf(jwtProvider.getUserId(token));

        UserDetails userDetails = userDetailsService.loadUserByUsername(userId);

        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
}
