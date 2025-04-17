package kr.co.yournews.common.util;

import static kr.co.yournews.common.util.AuthConstants.TOKEN_TYPE;

public class JwtUtil {

    /**
     * Token TYPE("Bearer ") 파싱 메서드
     * @param token : 토큰
     * @return : TYPE 파싱 토큰
     */
    public static String resolveToken(String token) {
        return token.substring(TOKEN_TYPE.length()).trim();
    }
}
