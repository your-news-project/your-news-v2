package kr.co.yournews.apis.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class UsernameDto {

    public record Request(
            @NotBlank(message = "이메일은 필수 입력 값입니다.")
            @Email(message = "이메일 형식이 아닙니다.")
            String email
    ) { }

    public record Response(
            String username
    ) {
        public static Response from(String username) {
            return new Response(username);
        }
    }
}

