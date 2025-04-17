package kr.co.yournews.infra.mail.strategy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static kr.co.yournews.infra.mail.util.MailConstants.PASS_SUBJECT;
import static kr.co.yournews.infra.mail.util.MailConstants.PASS_TEXT;

@Component
public class PassMailStrategy implements MailStrategy {

    @Value("${mail.reset-password.link}")
    private String resetPasswordLink;

    @Override
    public String getSubject() {
        return PASS_SUBJECT;
    }

    @Override
    public String generateContent(String uuid) {
        return PASS_TEXT + "<p><a href=\"" + resetPasswordLink + "?code=" + uuid + "\">비밀번호 재설정</a></p>";
    }
}
