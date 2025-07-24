package kr.co.yournews.infra.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import kr.co.yournews.infra.mail.strategy.MailStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;

import static kr.co.yournews.infra.mail.util.MailConstants.SENDER;

@Slf4j
@Component
@RequiredArgsConstructor
public class MailSenderAdapter {
    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String mailSender;

    /* 메일 보내기 */
    @Async(value = "notificationExecutor")
    public void sendMail(String email, String content, MailStrategy strategy) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(email);
            helper.setSubject(strategy.getSubject());
            helper.setText(strategy.generateContent(content), true);
            helper.setFrom(new InternetAddress(mailSender, SENDER, "UTF-8"));

            javaMailSender.send(message);
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("Failed to send email to {} with subject '{}' | Reason: {}",
                    email, strategy.getSubject(), e.getMessage());
        }
    }
}

