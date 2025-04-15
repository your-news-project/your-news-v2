package kr.co.yournews.apis.auth.service;

import kr.co.yournews.domain.user.type.OAuthPlatform;
import kr.co.yournews.infra.oauth.KakaoOAuthClient;
import kr.co.yournews.infra.oauth.NaverOAuthClient;
import kr.co.yournews.infra.oauth.OAuthClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuthClientFactory {
    private final NaverOAuthClient naverOAuthClient;
    private final KakaoOAuthClient kakaoOAuthClient;

    /**
     * OAuth 플랫폼 선택 팩토리 메서드
     *
     * @param platform : 접근하고자 하는 플랫폼
     * @return : 해당 플랫폼 객체 반환
     */
    public OAuthClient getPlatformService(OAuthPlatform platform) {
        return switch (platform) {
            case NAVER -> naverOAuthClient;
            case KAKAO -> kakaoOAuthClient;
        };
    }
}
