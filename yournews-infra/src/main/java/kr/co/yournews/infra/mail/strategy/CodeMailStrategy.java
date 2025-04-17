package kr.co.yournews.infra.mail.strategy;

import org.springframework.stereotype.Component;

import static kr.co.yournews.infra.mail.util.MailConstants.CODE_SUBJECT;
import static kr.co.yournews.infra.mail.util.MailConstants.CODE_TEXT;

@Component
public class CodeMailStrategy implements MailStrategy {

    @Override
    public String getSubject() {
        return CODE_SUBJECT;
    }

    @Override
    public String generateContent(String code) {
        return CODE_TEXT + code;
    }
}
