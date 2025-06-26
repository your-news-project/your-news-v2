package kr.co.yournews.apis.mail.controller;

import jakarta.validation.Valid;
import kr.co.yournews.apis.mail.dto.InquiryMailReq;
import kr.co.yournews.apis.mail.service.InquiryMailer;
import kr.co.yournews.common.response.success.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/mail")
@RequiredArgsConstructor
public class MailController {

    private final InquiryMailer inquiryMailer;

    @PostMapping("/inquiry")
    public ResponseEntity<?> sendInquiry(@RequestBody @Valid InquiryMailReq inquiryMailReq) {
        inquiryMailer.sendInquiryMail(inquiryMailReq);
        return ResponseEntity.ok(SuccessResponse.ok());
    }
}
