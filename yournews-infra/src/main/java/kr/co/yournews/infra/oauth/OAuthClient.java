package kr.co.yournews.infra.oauth;

import kr.co.yournews.infra.oauth.dto.OAuthUserInfoRes;

public interface OAuthClient {
    /**
     * OAuth 로그인 처리 메서드
     */
    OAuthUserInfoRes authenticate(String code);
}
