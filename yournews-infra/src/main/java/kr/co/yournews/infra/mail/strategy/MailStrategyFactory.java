package kr.co.yournews.infra.mail.strategy;

import kr.co.yournews.infra.mail.type.MailType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MailStrategyFactory {
    private final CodeMailStrategy codeMailStrategy;

    public MailStrategy getStrategy(MailType mailType) {
        return switch (mailType) {
            case CODE -> codeMailStrategy;
            case PASS -> null;
        };
    }
}

