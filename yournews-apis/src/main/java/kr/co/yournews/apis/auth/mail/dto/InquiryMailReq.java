package kr.co.yournews.apis.auth.mail.dto;

import jakarta.validation.constraints.NotBlank;

public record InquiryMailReq(
        String email,

        @NotBlank(message = "문의 내용을 입력해주세요.")
        String message
) {
}
