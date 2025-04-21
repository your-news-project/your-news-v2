package kr.co.yournews.infra.mail.strategy;

import org.springframework.stereotype.Component;

import static kr.co.yournews.infra.mail.util.MailConstants.INQUIRY_SUBJECT;
import static kr.co.yournews.infra.mail.util.MailConstants.INQUIRY_TEXT;

@Component
public class InquiryMailStrategy implements MailStrategy {

    @Override
    public String getSubject() {
        return INQUIRY_SUBJECT;
    }

    @Override
    public String generateContent(String content) {
        return INQUIRY_TEXT + content;
    }
}
