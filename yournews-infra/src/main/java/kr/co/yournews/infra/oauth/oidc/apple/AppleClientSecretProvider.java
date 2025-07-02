package kr.co.yournews.infra.oauth.oidc.apple;

import io.jsonwebtoken.Jwts;
import kr.co.yournews.infra.properties.OAuthProperties;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

@Component
public class AppleClientSecretProvider {

    /**
     * Apple 플랫폼의 OAuth Client Secret을 생성하는 메서드
     * - Apple의 경우 client_secret을 JWT로 생성하여 전달해야 함
     * - client_secret는 180일 동안 유효하므로, 캐싱하여 사
     *
     * @param properties : OAuth 플랫폼 설정 (ApplePlatform 타입이어야 함)
     * @return : Apple용 client_secret
     */
    @Cacheable(cacheNames = "appleClientSecret", key = "#properties.clientId")
    public String createClientSecret(OAuthProperties.Platform properties) {
        if (properties instanceof OAuthProperties.ApplePlatform appleProps) {
            return createAppleClientSecret(appleProps);
        }
        throw new IllegalArgumentException("Apple platform configuration is required.");
    }

    /**
     * Apple 전용 client_secret JWT를 생성하는 메서드
     * - header: alg(ES256), kid
     * - payload: iss, sub, aud, iat, exp
     * - 서명: Apple에서 발급받은 private key로 ES256 방식 서명
     *
     * @param appleProps : Apple 플랫폼 설정
     * @return : JWT 형식의 client_secret
     */
    private String createAppleClientSecret(OAuthProperties.ApplePlatform appleProps) {
        Instant now = Instant.now();

        return Jwts.builder()
                .header()
                .add("alg", "ES256")
                .add("kid", appleProps.getKeyId())
                .and()
                .issuer(appleProps.getTeamId())
                .subject(appleProps.getClientId())
                .audience().add(appleProps.getIssuer()).and()
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(60 * 60 * 24 * 180)))
                .signWith(getPrivateKey(appleProps.getPrivateKey()))
                .compact();
    }


    /**
     * Apple에서 발급받은 private key 문자열을 Java PrivateKey 객체로 변환
     *
     * @param rawPrivateKey : Base64 인코딩된 개인키 문자열
     * @return : 변환된 PrivateKey 객체
     */
    private PrivateKey getPrivateKey(String rawPrivateKey) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(rawPrivateKey);

            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");    // Apple은 EC 키 사용
            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid Apple private key", e);
        }
    }
}
