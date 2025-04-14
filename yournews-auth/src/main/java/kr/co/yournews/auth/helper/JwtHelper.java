package kr.co.yournews.auth.helper;

import kr.co.yournews.auth.dto.TokenDto;
import kr.co.yournews.auth.jwt.provider.JwtProvider;
import kr.co.yournews.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtHelper {
    private final JwtProvider jwtProvider;

    public TokenDto createToken(User user) {
        Long userId = user.getId();
        String username = user.getUsername();
        String nickname = user.getNickname();

        String accessToken = jwtProvider.generateAccessToken(username, nickname, userId);

        return TokenDto.of(accessToken);
    }
}
