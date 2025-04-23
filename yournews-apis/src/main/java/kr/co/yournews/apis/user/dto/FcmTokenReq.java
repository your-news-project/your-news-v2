package kr.co.yournews.apis.user.dto;

import kr.co.yournews.domain.user.entity.FcmToken;
import kr.co.yournews.domain.user.entity.User;

public class FcmTokenReq {

    public record Register(
            String token,
            String deviceInfo
    ) {
        public FcmToken toEntity(User user) {
            return FcmToken.builder()
                    .token(token)
                    .deviceInfo(deviceInfo)
                    .user(user)
                    .build();
        }
    }

    public record Delete(
            String deviceInfo
    ) { }
}
