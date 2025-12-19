package kr.co.yournews.apis.subscription.webhook.google;

import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.util.concurrent.TimeUnit;

/**
 * Google RTDN(Webhook) 요청의 OIDC Bearer 토큰을 검증하는 클래스.
 */
@Component
public class GoogleOidcVerifier {
    private final GoogleRtdnProperties properties;
    private final JwkProvider jwkProvider;

    /**
     * Google JWKS URI를 기반으로 OIDC 검증기를 생성.
     */
    public GoogleOidcVerifier(GoogleRtdnProperties properties) {
        this.properties = properties;
        try {
            this.jwkProvider = new JwkProviderBuilder(new URL(properties.getJwksUri()))
                    .cached(10, 24, TimeUnit.HOURS)
                    .rateLimited(10, 1, TimeUnit.MINUTES)
                    .build();
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Invalid google.webhook.jwks-uri", e);
        }
    }

    /**
     * Authorization 헤더의 Bearer 토큰을 검증하는 메서드.
     *
     * @param authorizationHeader : Authorization 헤더 값
     */
    public void verifyBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new JWTVerificationException("Missing Authorization Bearer token");
        }
        String token = authorizationHeader.substring("Bearer ".length());
        verify(token);
    }

    /**
     * Google OIDC JWT 토큰의 서명과 클레임을 검증하는 메서드.
     *
     * @param token : Bearer 토큰 문자열
     */
    private void verify(String token) {
        DecodedJWT decoded = JWT.decode(token);

        String kid = decoded.getKeyId();
        if (kid == null) {
            throw new JWTVerificationException("Missing kid");
        }

        RSAPublicKey publicKey;
        try {
            publicKey = (RSAPublicKey) jwkProvider.get(kid).getPublicKey();
        } catch (Exception e) {
            throw new JWTVerificationException("Failed to load public key", e);
        }

        Algorithm algorithm = Algorithm.RSA256(publicKey, null);

        JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer("https://accounts.google.com", "accounts.google.com")
                .withAudience(properties.getAudience())
                .build();

        DecodedJWT jwt = verifier.verify(token);

        String email = jwt.getClaim("email").asString();
        if (!properties.getEmail().equals(email)) {
            throw new JWTVerificationException("Invalid service account email");
        }
    }
}
