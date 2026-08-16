package kr.co.yournews.apis.studentservice.service;

import kr.co.yournews.apis.studentservice.event.StudentServiceRegisteredEvent;
import kr.co.yournews.infra.mail.MailSenderAdapter;
import kr.co.yournews.infra.mail.strategy.MailStrategyFactory;
import kr.co.yournews.infra.mail.type.MailType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.util.HtmlUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class StudentServiceRegistrationMailer {
    private final MailSenderAdapter mailSenderAdapter;
    private final MailStrategyFactory mailStrategyFactory;

    @Value("${mail.admin.email}")
    private String adminEmail;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendRegistrationNotification(StudentServiceRegisteredEvent event) {
        String content = "서비스 ID: " + event.studentServiceId() + "<br/>"
                + "서비스명: " + escape(event.name()) + "<br/>"
                + "게시물 유형: " + event.contentType() + "<br/>"
                + "등록자: " + escape(event.registrantNickname()) + " ("
                + escape(event.registrantEmail()) + ")<br/>"
                + "이미지 수: " + event.imageCount() + "장<br/>"
                + "링크: " + escapeUrls(event.serviceUrls()) + "<br/><br/>"
                + "소개:<br/>" + escape(event.description()).replace("\n", "<br/>");

        mailSenderAdapter.sendMail(
                adminEmail,
                content,
                mailStrategyFactory.getStrategy(MailType.STUDENT_SERVICE_REGISTRATION)
        );
        log.info("[학생 서비스 등록 알림 요청 완료] studentServiceId: {}", event.studentServiceId());
    }

    private String escape(String value) {
        return HtmlUtils.htmlEscape(value == null ? "" : value);
    }

    private String escapeUrls(java.util.List<String> urls) {
        return urls == null || urls.isEmpty()
                ? "없음"
                : urls.stream().map(this::escape).collect(java.util.stream.Collectors.joining("<br/>"));
    }
}
