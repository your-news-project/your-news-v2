package kr.co.yournews.infra.iap.apple;

import com.apple.itunes.storekit.client.AppStoreServerAPIClient;
import com.apple.itunes.storekit.model.Data;
import com.apple.itunes.storekit.model.JWSTransactionDecodedPayload;
import com.apple.itunes.storekit.model.ResponseBodyV2DecodedPayload;
import com.apple.itunes.storekit.model.TransactionInfoResponse;
import com.apple.itunes.storekit.verification.SignedDataVerifier;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.yournews.infra.iap.dto.AppleTransactionDecoded;
import kr.co.yournews.infra.iap.dto.AppleServerNotificationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppleAppStoreClient {
    private final AppStoreServerAPIClient client;
    private final SignedDataVerifier verifier;
    private final ObjectMapper objectMapper;

    /**
     * transactionId로 Apple의 signed transaction 정보를 조회하는 메서드.
     *
     * @param transactionId : Apple App Store에서 발급한 트랜잭션 ID
     * @return : 서명된 transaction 정보
     */
    public String getTransactionInfo(String transactionId) {
        try {
            TransactionInfoResponse response = client.getTransactionInfo(transactionId);
            return response.getSignedTransactionInfo();
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to get transaction info from Apple. transactionId= " + transactionId,
                    e
            );
        }
    }

    /**
     * signed transaction(JWS)을 검증하고 디코딩하는 메서드.
     *
     * @param signedTransactionInfo : Apple에서 전달된 서명된 transaction 정보
     * @return : 검증 및 디코딩된 Apple 트랜잭션 정보
     */
    public AppleTransactionDecoded decodeTransaction(String signedTransactionInfo) {
        try {
            JWSTransactionDecodedPayload decodedPayload =
                    verifier.verifyAndDecodeTransaction(signedTransactionInfo);

            return AppleTransactionDecoded.of(
                    decodedPayload.getOriginalTransactionId(),
                    decodedPayload.getTransactionId(),
                    decodedPayload.getTransactionReason().getValue(),
                    decodedPayload.getProductId(),
                    decodedPayload.getPurchaseDate(),
                    decodedPayload.getExpiresDate()
            );
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to verify or decode Apple signed transaction",
                    e
            );
        }
    }

    /**
     * Apple 서버 Webhook을 검증하고 디코딩하는 메서드
     *
     * @param body : Apple 서버로부터 전달받은 webhook 요청 본문
     * @return : 디코딩된 Apple 서버 알림 데이터
     */
    public AppleServerNotificationDto decodeWebhookTransaction(String body) {
        try {
            JsonNode root = objectMapper.readTree(body);
            String signedPayload = root.get("signedPayload").asText();

            ResponseBodyV2DecodedPayload decodedPayload = verifier.verifyAndDecodeNotification(signedPayload);

            String notificationType = decodedPayload.getNotificationType().getValue();
            String subtype = decodedPayload.getSubtype() == null
                    ? null
                    : decodedPayload.getSubtype().getValue();

            Data data = decodedPayload.getData();
            String signedTransactionInfo = data.getSignedTransactionInfo();

            AppleTransactionDecoded transaction =
                    decodeTransaction(signedTransactionInfo);

            return AppleServerNotificationDto.of(notificationType, subtype, transaction);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to decode Apple server notification payload",
                    e
            );
        }
    }
}
