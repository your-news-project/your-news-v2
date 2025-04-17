package kr.co.yournews.apis.auth.dto;

public class AuthCodeDto {

    public record Request(String email) {
    }

    public record Verify(String email, String code) {
    }
}
