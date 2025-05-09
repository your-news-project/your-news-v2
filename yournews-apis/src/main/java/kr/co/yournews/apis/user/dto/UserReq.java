package kr.co.yournews.apis.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class UserReq {

    public record UpdatePassword(
            @NotBlank(message = "현재 비밀번호를 입력해주세요.")
            String currentPassword,

            @NotBlank(message = "새로운 비밀번호를 입력해주세요.")
            @Pattern(regexp = "(?=.*[0-9])(?=.*[a-zA-Z])(?=.*\\W).{8,16}", message = "비밀번호는 8~16자 영문, 숫자, 특수문자를 사용하세요.")
            String newPassword
    ) {
    }

    public record UpdateProfile(
            @NotBlank(message = "닉네임은 필수 입력 값입니다.")
            @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-zA-Z0-9-_]{2,10}$", message = "닉네임은 특수문자를 제외한 2~10자리여야 합니다.")
            String nickname
    ) {
    }

    public record UpdateStatus(
            boolean subStatus,
            boolean dailySubStatus
    ) {
    }
}
