package kr.co.yournews.infra.oauth.oidc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import kr.co.yournews.infra.oauth.dto.oidc.OidcDecodePayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtOidcProvider {
    private final ObjectMapper objectMapper;

    public String getKidFromUnsignedTokenHeader(String token, String iss, String aud) {
        return getUnsignedTokenClaims(token, iss, aud).get("header").get("kid");
    }

    /**
     * ID Token의 header와 body를 Base64 방식으로 디코딩하는 메서드
     * payload의 iss, aud를 검증하고, 실패시 예외 처리
     *
     * @param token : id token
     * @param iss   : 기대하는 issuer
     * @param aud   : 기대하는 audience (clientId)
     * @return : header, payload를 포함한 Map 구조
     */
    private Map<String, Map<String, String>> getUnsignedTokenClaims(String token, String iss, String aud) {
        try {
            Base64.Decoder decoder = Base64.getUrlDecoder();
            String unsignedToken = getUnsignedToken(token);

            String headerJson = new String(decoder.decode(unsignedToken.split("\\.")[0]));
            Map<String, String> header = objectMapper.readValue(headerJson, Map.class);

            String payloadJson = new String(decoder.decode(unsignedToken.split("\\.")[1]));
            Map<String, String> payload = objectMapper.readValue(payloadJson, Map.class);

            // iss 검증
            if (!iss.equals(payload.get("iss"))) {
                throw new IllegalArgumentException("iss mismatch. expected: " + iss + ", actual: " + payload.get("iss"));
            }

            // aud 검증
            if (!aud.equals(payload.get("aud"))) {
                throw new IllegalArgumentException("aud mismatch. expected: " + aud + ", actual: " + payload.get("aud"));
            }

            return Map.of("header", header, "payload", payload);

        } catch (JsonProcessingException | IllegalArgumentException e) {
            throw new IllegalArgumentException("getUnsignedTokenClaims error" + e.getMessage());
        }
    }

    /**
     * JWT 토큰의 signature를 제거한 형태를 반환하는 메서드
     * - 헤더, 페이로드의 claim 추출에 사용
     *
     * @param token : 서명을 포함한 JWT (ex. header.payload.signature)
     * @return : unsigned JWT (ex. header.payload.)
     */
    private String getUnsignedToken(String token) {
        String[] splitToken = token.split("\\.");
        if (splitToken.length != 3) {
            throw new IllegalArgumentException("Invalid JWT format");
        }
        return splitToken[0] + "." + splitToken[1] + ".";
    }

    /**
     * 서명이 검증된 JWT로부터 Claims(Payload)를 추출하고,
     * 그 중 필요한 필드(iss, aud, sub, email)를 반환하는 메서드
     *
     * @param token    : id token
     * @param modulus  : 공개키의 modulus (Base64)
     * @param exponent : 공개키의 exponent (Base64)
     * @return : 서명 검증된 사용자 식별 정보
     */
    public OidcDecodePayload getOIDCTokenBody(String token, String modulus, String exponent) {
        Claims body = getOIDCTokenJws(token, modulus, exponent).getPayload();
        String aud = body.getAudience().iterator().next(); // aud가 여러개일 경우 첫 번째
        return new OidcDecodePayload(
                body.getIssuer(),
                aud,
                body.getSubject(),
                body.get("email", String.class)
        );
    }

    /**
     * 공개키로 서명을 검증하는 메서드
     */
    private Jws<Claims> getOIDCTokenJws(String token, String modulus, String exponent) {
        try {
            return Jwts.parser()
                    .verifyWith(getRSAPublicKey(modulus, exponent))
                    .build()
                    .parseSignedClaims(token);
        } catch (JwtException e) {
            throw new IllegalArgumentException("Invalid JWT signature");
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalArgumentException("JWT parsing failed: " + e.getMessage(), e);
        }
    }

    /**
     * n, e 조합으로 공개키를 생성하는 메서드
     */
    private PublicKey getRSAPublicKey(String modulus, String exponent) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        byte[] decodeN = Base64.getUrlDecoder().decode(modulus);
        byte[] decodeE = Base64.getUrlDecoder().decode(exponent);
        BigInteger n = new BigInteger(1, decodeN);
        BigInteger e = new BigInteger(1, decodeE);

        RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(n, e);
        return keyFactory.generatePublic(publicKeySpec);
    }
}
