package kr.co.yournews.admin.user.dto;

import jakarta.validation.constraints.NotBlank;

public record UserWithdrawDto(
        @NotBlank(message = "밴 이유는 필수 입력 값입니다.")
        String reason
) {
}
