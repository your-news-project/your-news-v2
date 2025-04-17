package kr.co.yournews.infra.mail.strategy;

public interface MailStrategy {
    String getSubject();
    String generateContent(String content);
}
