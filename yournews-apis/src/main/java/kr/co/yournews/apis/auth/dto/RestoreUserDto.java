package kr.co.yournews.apis.auth.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class RestoreUserDto {

    public record Request(
            String username
    ) { }

    public record AuthErrorData(
            LocalDate deletedAt
    ) {
        public static AuthErrorData of(LocalDateTime deletedAt) {
            return new AuthErrorData(deletedAt.toLocalDate().plusDays(14));
        }
    }

    public record OAuthErrorData(
            String username,
            LocalDate deletedAt
    ) {
        public static OAuthErrorData of(String username, LocalDateTime deletedAt) {
            return new OAuthErrorData(username, deletedAt.toLocalDate().plusDays(14));
        }
    }
}
