package kr.co.yournews.infra.mail.strategy;

import org.springframework.stereotype.Component;

import static kr.co.yournews.infra.mail.util.MailConstants.STUDENT_SERVICE_SUBJECT;
import static kr.co.yournews.infra.mail.util.MailConstants.STUDENT_SERVICE_TEXT;

@Component
public class StudentServiceRegistrationMailStrategy implements MailStrategy {

    @Override
    public String getSubject() {
        return STUDENT_SERVICE_SUBJECT;
    }

    @Override
    public String generateContent(String content) {
        return STUDENT_SERVICE_TEXT + content;
    }
}
