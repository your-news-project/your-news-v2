package kr.co.yournews.apis.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class PassResetDto {

    public record VerifyUser(
            String username,
            String email
    ) { }

    public record ResetPassword(
            @NotBlank(message = "아이디는 필수 입력 값입니다.")
            String username,
            String uuid,
            @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
            @Pattern(regexp = "(?=.*[0-9])(?=.*[a-zA-Z])(?=.*\\W).{8,16}", message = "비밀번호는 8~16자 영문, 숫자, 특수문자를 사용하세요.")
            String password
    ) { }
}
