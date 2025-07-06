package kr.co.yournews.apis.mail.service;

import kr.co.yournews.apis.mail.dto.InquiryMailReq;
import kr.co.yournews.infra.mail.MailSenderAdapter;
import kr.co.yournews.infra.mail.strategy.MailStrategyFactory;
import kr.co.yournews.infra.mail.type.MailType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class InquiryMailer {
    private final MailSenderAdapter mailSenderAdapter;
    private final MailStrategyFactory mailStrategyFactory;

    @Value("${mail.admin.email}")
    private String email;

    /**
     * 사용자로부터 받은 문의 내용을 관리자에게 이메일로 전송하는 메서드
     *
     * @param inquiryMailReq : 문의자의 이메일과 메시지를 포함한 요청 객체
     */
    public void sendInquiryMail(InquiryMailReq inquiryMailReq) {
        log.info("[문의 메일 요청] from: {}", inquiryMailReq.email());

        String content = inquiryMailReq.email() + "<br>" + inquiryMailReq.message();

        mailSenderAdapter.sendMail(
                email,
                content,
                mailStrategyFactory.getStrategy(MailType.INQUIRY)
        );

        log.info("[문의 메일 전송 완료] from: {}", inquiryMailReq.email());
    }
}
