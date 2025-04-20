package kr.co.yournews.infra.redis.util;

public final class RedisConstants {

    private RedisConstants() { }

    public static final String REFRESH_TOKEN_PREFIX = "auth:refresh::";
    public static final String CODE_PREFIX = "auth:code::";
    public static final String VERIFIED_PREFIX = "auth:verified::";
    public static final String BLACKLIST_KEY_PREFIX = "auth:blacklist::";
    public static final String PASS_KEY_PREFIX = "auth:passcode::";
    public static final String PROCESSED_URL_PREFIX = "processed-url::";
}
