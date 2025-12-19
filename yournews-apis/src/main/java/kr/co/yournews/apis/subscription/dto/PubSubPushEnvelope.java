package kr.co.yournews.apis.subscription.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PubSubPushEnvelope(
        Message message,
        String subscription
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Message(
            String data,
            String messageId,
            String publishTime
    ) { }
}
