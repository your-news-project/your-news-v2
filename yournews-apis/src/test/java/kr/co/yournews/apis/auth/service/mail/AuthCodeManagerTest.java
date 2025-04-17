package kr.co.yournews.apis.auth.service.mail;

import kr.co.yournews.apis.auth.dto.AuthCodeDto;
import kr.co.yournews.infra.mail.MailSenderAdapter;
import kr.co.yournews.infra.mail.strategy.MailStrategy;
import kr.co.yournews.infra.mail.strategy.MailStrategyFactory;
import kr.co.yournews.infra.mail.type.MailType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthCodeManagerTest {

    @Mock
    private AuthCodeService authCodeService;

    @Mock
    private MailSenderAdapter mailSenderAdapter;

    @Mock
    private MailStrategyFactory mailStrategyFactory;

    @InjectMocks
    private AuthCodeManager authCodeManager;


    @Test
    @DisplayName("인증 코드 전송 테스트")
    void sendAuthCodeTest() {
        // given
        String email = "test@example.com";
        String code = "123456";
        AuthCodeDto.Request dto = new AuthCodeDto.Request(email);

        MailStrategy mockStrategy = mock(MailStrategy.class);
        when(authCodeService.generateAndSave(email)).thenReturn(code);
        when(mailStrategyFactory.getStrategy(MailType.CODE)).thenReturn(mockStrategy);

        // when
        authCodeManager.sendAuthCode(dto);

        // then
        verify(authCodeService).generateAndSave(email);
        verify(mailStrategyFactory).getStrategy(MailType.CODE);
        verify(mailSenderAdapter).sendMail(email, code, mockStrategy);
    }

    @Test
    @DisplayName("인증 코드 검증 성공 테스트")
    void verifyAuthCodeTest() {
        // given
        String email = "test@example.com";
        String code = "123456";
        AuthCodeDto.Verify dto = new AuthCodeDto.Verify(email, code);

        when(authCodeService.verifiedCode(email, code)).thenReturn(true);

        // when
        boolean result = authCodeManager.verifyAuthCode(dto);

        // then
        verify(authCodeService).verifiedCode(email, code);
    }
}
