package kr.co.yournews.apis.subscription.service;

import kr.co.yournews.apis.subscription.dto.SubscriptionDto;
import kr.co.yournews.common.response.exception.CustomException;
import kr.co.yournews.common.util.DateTimeConvertUtil;
import kr.co.yournews.domain.user.entity.Subscription;
import kr.co.yournews.domain.user.entity.User;
import kr.co.yournews.domain.user.exception.SubscriptionErrorType;
import kr.co.yournews.domain.user.exception.UserErrorType;
import kr.co.yournews.domain.user.service.SubscriptionService;
import kr.co.yournews.domain.user.service.UserService;
import kr.co.yournews.domain.user.type.SubscriptionPlatform;
import kr.co.yournews.domain.user.type.SubscriptionStatus;
import kr.co.yournews.infra.iap.apple.AppleAppStoreClient;
import kr.co.yournews.infra.iap.dto.GooglePlaySubscriptionInfo;
import kr.co.yournews.infra.iap.dto.AppleTransactionDecoded;
import kr.co.yournews.infra.iap.google.GooglePlayClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionCommandService {
    private final SubscriptionService subscriptionService;
    private final AppleAppStoreClient appleAppStoreClient;
    private final GooglePlayClient googlePlayClient;
    private final UserService userService;

    @Value("${google-iap.productId}")
    private String googleProductId;

    @Value("${google-iap.packageName}")
    private String googlePackageName;

    @Value("${apple-iap.productId}")
    private String appleProductId;

    /**
     * Apple 결제 요청을 검증하고 구독을 저장하는 메서드.
     * - transactionId로 Apple 서버에서 signedTransactionInfo 조회
     * - 서명 검증 및 디코딩 후, productId/transactionId 일치 여부 검증
     * - 구독 정보 저장
     *
     * @param userId  : 사용자 ID
     * @param request : Apple 결제 확인 요청
     */
    @Transactional
    public void validateAndCreateAppleSubscription(
            Long userId,
            SubscriptionDto.AppleConfirmRequest request
    ) {
        AppleTransactionDecoded resultDto = fetchAndDecodeAppleTransaction(request.transactionId());
        validateAppleConfirmRequest(request.transactionId(), resultDto);

        saveSubscription(
                userId, resultDto.purchaseDate(), resultDto.expiresDate(),
                resultDto.originalTransactionId(),
                resultDto.productId(), SubscriptionPlatform.APPLE
        );

        log.info("[Apple Purchase] userId={}, transactionId={}",
                userId, resultDto.originalTransactionId());
    }

    /**
     * Apple 구독 복원을 처리하는 메서드.
     * - expiresDate가 가장 늦은(가장 최신) 트랜잭션을 선택
     * - 동일 originalTransactionId 구독이 존재하면:
     *      - 다른 유저 소유면 예외
     *      - 기존 만료일보다 최신이 아니면 무시
     * - 없거나 최신이면 저장
     *
     * @param userId  : 사용자 ID
     * @param request : Apple 복원 요청
     */
    @Transactional
    public void restoreAppleSubscription(
            Long userId,
            SubscriptionDto.AppleRestoreRequest request
    ) {
        if (request.transactionIds() == null || request.transactionIds().isEmpty()) {
            return;
        }

        // 전달된 transactionId 목록을 Apple 서버에서 조회 후 디코딩
        List<AppleTransactionDecoded> decodedList = request.transactionIds().stream()
                .map(this::fetchAndDecodeAppleTransaction)
                .filter(dto -> appleProductId.equals(dto.productId()))
                .toList();

        if (decodedList.isEmpty()) {
            return;
        }

        // 만료일이 가장 늦은 트랜잭션을 선택
        AppleTransactionDecoded resultDto = decodedList.stream()
                .max(Comparator.comparing(AppleTransactionDecoded::expiresDate))
                .orElseThrow();

        Subscription subscription =
                subscriptionService.readTopBySubscriptionIdOrderByCreatedAtDesc(resultDto.originalTransactionId())
                        .orElse(null);

        LocalDateTime expireAt =
                DateTimeConvertUtil.epochMillisToLocalDateTime(resultDto.expiresDate());

        if (subscription != null) {
            // 동일 originalTransactionId가 다른 유저에게 연결된 경우 예외
            if (!subscription.getUser().getId().equals(userId)) {
                throw new CustomException(
                        SubscriptionErrorType.SUBSCRIPTION_OWNED_BY_ANOTHER_USER,
                        subscription.getUser().getEmail()
                );
            }

            // 기존 구독의 만료일이 더 최신이거나 같으면 갱신하지 않음
            if (!expireAt.isAfter(subscription.getExpireAt())) {
                return;
            }
        }

        saveSubscription(
                userId, resultDto.purchaseDate(), resultDto.expiresDate(),
                resultDto.originalTransactionId(),
                resultDto.productId(), SubscriptionPlatform.APPLE
        );

        log.info("[Apple Restore] userId={}, transactionId={}",
                userId, resultDto.originalTransactionId());
    }

    /**
     * Apple transactionId로 서버에서 트랜잭션 정보를 조회하고 디코딩하는 메서드.
     *
     * @param transactionId : Apple 트랜잭션 ID
     * @return 디코딩된 트랜잭션 정보
     */
    private AppleTransactionDecoded fetchAndDecodeAppleTransaction(String transactionId) {
        String signedTransactionInfo = appleAppStoreClient.getTransactionInfo(transactionId);
        return appleAppStoreClient.decodeTransaction(signedTransactionInfo);
    }

    /**
     * Apple 결제 Confirm 요청의 유효성을 검증하는 메서드.
     * - 서버 디코딩 결과 productId가 등록된 상품인지 확인
     * - 요청 transactionId와 디코딩 transactionId가 일치하는지 확인
     *
     * @param transactionId : 요청으로 전달된 transactionId
     * @param resultDto     : 디코딩된 트랜잭션 정보
     */
    private void validateAppleConfirmRequest(
            String transactionId,
            AppleTransactionDecoded resultDto
    ) {
        if (!appleProductId.equals(resultDto.productId())) {
            throw new IllegalArgumentException("Invalid productId reason");
        }

        if (!transactionId.equals(resultDto.transactionId())) {
            throw new IllegalArgumentException("TransactionId mismatch");
        }
    }

    /**
     * Google 결제 요청을 검증하고 구독을 저장하는 메서드.
     * - packageName/productId/purchaseToken로 검증
     * - 구독 정보 저장
     *
     * @param userId  : 사용자 ID
     * @param request : Google 결제 확인 요청
     */
    @Transactional
    public void validateAndCreateGoogleSubscription(Long userId, SubscriptionDto.GoogleConfirmRequest request) {
        validateGoogleConfirmRequest(request);

        // Google Play API를 통해 실제 구독 정보 조회
        GooglePlaySubscriptionInfo purchase = googlePlayClient.getSubscription(
                request.packageName(),
                request.productId(),
                request.purchaseToken()
        );

        saveSubscription(
                userId, purchase.purchaseDate(), purchase.expiresDate(),
                request.purchaseToken(),
                request.productId(), SubscriptionPlatform.GOOGLE
        );

        log.info("[Google Purchase] userId={}, purchaseToken={}", userId, request.purchaseToken());
    }

    /**
     * Google 결제 요청의 기본 필드 유효성을 검증하는 메서드.
     * - packageName / productId가 서버 설정 값과 일치하는지 확인
     * - purchaseToken이 비어있지 않은지 확인
     *
     * @param request : Google 결제 확인 요청
     */
    private void validateGoogleConfirmRequest(SubscriptionDto.GoogleConfirmRequest request) {
        if (!googlePackageName.equals(request.packageName())) {
            throw new IllegalArgumentException("Invalid packageName");
        }
        if (!googleProductId.equals(request.productId())) {
            throw new IllegalArgumentException("Invalid productId");
        }
        if (request.purchaseToken() == null || request.purchaseToken().isBlank()) {
            throw new IllegalArgumentException("purchaseToken is blank");
        }
    }

    /**
     * Apple 트랜잭션 정보를 DB에 저장하는 메서드
     * - expiresDate 기준으로 ACTIVE / EXPIRED 상태 결정
     *
     * @param userId          : 사용자 ID
     * @param purchaseDate    : 구매 시각
     * @param expiresDate     : 만료 시각
     * @param subscriptionId  : 구독 식별자 (Apple: originalTransactionId, Google: purchaseToken)
     * @param productId       : 상품 ID
     * @param platform        : 구독 플랫폼 (APPLE / GOOGLE)
     */
    private void saveSubscription(
            Long userId,
            Long purchaseDate,
            Long expiresDate,
            String subscriptionId,
            String productId,
            SubscriptionPlatform platform
    ) {
        LocalDateTime expireAt =
                DateTimeConvertUtil.epochMillisToLocalDateTime(expiresDate);

        User user = userService.readById(userId)
                .orElseThrow(() -> new CustomException(UserErrorType.NOT_FOUND));

        Subscription subscription = Subscription.builder()
                .subscriptionId(subscriptionId)
                .productId(productId)
                .platform(platform)
                .purchaseAt(
                        DateTimeConvertUtil.epochMillisToLocalDateTime(purchaseDate)
                )
                .expireAt(expireAt)
                .status(
                        expireAt.isAfter(LocalDateTime.now())
                                ? SubscriptionStatus.ACTIVE
                                : SubscriptionStatus.EXPIRED
                )
                .user(user)
                .build();

        subscriptionService.save(subscription);
    }

}
