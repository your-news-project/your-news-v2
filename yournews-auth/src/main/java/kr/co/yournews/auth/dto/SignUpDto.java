package kr.co.yournews.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import kr.co.yournews.domain.user.entity.User;
import kr.co.yournews.domain.user.type.Role;

import java.util.List;

public class SignUpDto {

    public record Auth(
            @NotBlank(message = "아이디는 필수 입력입니다.")
            @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-z0-9-_]{4,20}$", message = "아이디는 특수문자를 제외한 4~20자리여야 합니다.")
            String username,

            @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
            @Pattern(regexp = "(?=.*[0-9])(?=.*[a-zA-Z])(?=.*\\W).{8,16}", message = "비밀번호는 8~16자 영문, 숫자, 특수문자를 사용하세요.")
            String password,

            @NotBlank(message = "닉네임은 필수 입력 값입니다.")
            @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-zA-Z0-9-_]{2,10}$", message = "닉네임은 특수문자를 제외한 2~10자리여야 합니다.")
            String nickname,

            @NotBlank(message = "이메일은 필수 입력 값입니다.")
            @Email(message = "이메일 형식이 아닙니다.")
            String email,

            List<Long> newsIds,

            List<String> keywords,

            boolean subStatus
    ) {
        public User toEntity(String encodePassword) {
            return User.builder()
                    .username(username)
                    .password(encodePassword)
                    .nickname(nickname)
                    .email(email)
                    .signedUp(true)
                    .subStatus(subStatus)
                    .role(Role.USER)
                    .build();
        }
    }

    public record OAuth(
            @NotBlank(message = "닉네임은 필수 입력 값입니다.")
            @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-zA-Z0-9-_]{2,10}$", message = "닉네임은 특수문자를 제외한 2~10자리여야 합니다.")
            String nickname,

            List<Long> newsIds,

            List<String> keywords,

            boolean subStatus
    ) { }
}
