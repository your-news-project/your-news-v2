package kr.co.yournews.common.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 해시 유틸리티 클래스
 * - 입력 문자열을 고정 길이 해시 문자열로 변환
 * - 해시 알고리즘: MD5 (32자 고정)
 */
public class HashUtil {

    private static final String DEFAULT_ALGORITHM = "MD5";

    /**
     * URL → MD5 해시(32자 hex)
     */
    public static String hash(String str) {
        return digest(str);
    }

    /**
     * 내부 해시 생성 (hex 인코딩)
     */
    private static String digest(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance(DEFAULT_ALGORITHM);
            byte[] bytes = md.digest(input.getBytes(StandardCharsets.UTF_8));

            // byte[] → hex string
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Unsupported hash algorithm: " + DEFAULT_ALGORITHM, e);
        }
    }
}
