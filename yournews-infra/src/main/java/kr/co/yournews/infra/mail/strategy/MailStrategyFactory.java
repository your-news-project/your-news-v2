package kr.co.yournews.infra.mail.strategy;

import kr.co.yournews.infra.mail.type.MailType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MailStrategyFactory {
    private final CodeMailStrategy codeMailStrategy;
    private final PassMailStrategy passMailStrategy;
    private final InquiryMailStrategy inquiryMailStrategy;

    public MailStrategy getStrategy(MailType mailType) {
        return switch (mailType) {
            case CODE -> codeMailStrategy;
            case PASS -> passMailStrategy;
            case INQUIRY -> inquiryMailStrategy;
        };
    }
}

