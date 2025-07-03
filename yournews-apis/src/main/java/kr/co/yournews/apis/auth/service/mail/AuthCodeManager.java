package kr.co.yournews.apis.auth.service.mail;

import kr.co.yournews.apis.auth.dto.AuthCodeDto;
import kr.co.yournews.infra.mail.MailSenderAdapter;
import kr.co.yournews.infra.mail.strategy.MailStrategyFactory;
import kr.co.yournews.infra.mail.type.MailType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthCodeManager {
    private final AuthCodeService authCodeService;
    private final MailSenderAdapter mailSenderAdapter;
    private final MailStrategyFactory mailStrategyFactory;

    /**
     * 인증 코드를 생성하고 해당 이메일로 전송
     *
     * @param authCodeReq : 인증 코드 요청 객체 (이메일)
     */
    public void sendAuthCode(AuthCodeDto.Request authCodeReq) {
        log.info("[인증코드 요청] email={}", authCodeReq.email());

        String code = authCodeService.generateAndSave(authCodeReq.email());
        mailSenderAdapter.sendMail(
                authCodeReq.email(),
                code,
                mailStrategyFactory.getStrategy(MailType.CODE)
        );

        log.info("[인증코드 전송 완료] email={}", authCodeReq.email());
    }

    /**
     * 사용자가 입력한 인증 코드가 유효한지 검증
     *
     * @param authCodeDto 인증 코드 검증 요청 객체 (이메일, 코드)
     * @return : 인증 코드가 true
     */
    public boolean verifyAuthCode(AuthCodeDto.Verify authCodeDto) {
        return authCodeService.verifiedCode(authCodeDto.email(), authCodeDto.code());
    }
}
