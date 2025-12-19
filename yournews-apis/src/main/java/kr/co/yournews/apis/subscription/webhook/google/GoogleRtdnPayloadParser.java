package kr.co.yournews.apis.subscription.webhook.google;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.yournews.apis.subscription.dto.GoogleRtdnPayload;
import kr.co.yournews.apis.subscription.dto.PubSubPushEnvelope;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Google RTDN(Webhook) 요청 본문을 파싱하는 클래스.
 */
@Component
@RequiredArgsConstructor
public class GoogleRtdnPayloadParser {
    private final ObjectMapper objectMapper;

    /**
     * RTDN 요청 본문을 파싱하여 GoogleRtdnPayload로 변환하는 메서드.
     *
     * @param body : RTDN webhook 요청 본문
     * @return : 파싱된 Google RTDN payload
     */
    public GoogleRtdnPayload parse(String body) {
        try {
            if (isRawRtdn(body)) {
                return objectMapper.readValue(body, GoogleRtdnPayload.class);
            }

            PubSubPushEnvelope env = objectMapper.readValue(body, PubSubPushEnvelope.class);
            String decoded = decodeBase64(env.message().data());
            return objectMapper.readValue(decoded, GoogleRtdnPayload.class);

        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse Google RTDN payload", e);
        }
    }

    /**
     * 요청 본문이 Raw RTDN 형식인지 확인하는 메서드.
     */
    private boolean isRawRtdn(String body) {
        return body != null
                && body.trim().startsWith("{")
                && body.contains("\"subscriptionNotification\"");
    }

    /**
     * Base64 인코딩된 문자열을 디코딩하는 메서드.
     */
    private String decodeBase64(String base64) {
        return new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);
    }
}
