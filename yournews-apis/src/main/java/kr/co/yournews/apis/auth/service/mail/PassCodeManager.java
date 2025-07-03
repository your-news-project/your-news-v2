package kr.co.yournews.apis.auth.service.mail;

import kr.co.yournews.apis.auth.dto.PassResetDto;
import kr.co.yournews.auth.service.PasswordEncodeService;
import kr.co.yournews.common.response.exception.CustomException;
import kr.co.yournews.domain.user.entity.User;
import kr.co.yournews.domain.user.exception.UserErrorType;
import kr.co.yournews.domain.user.service.UserService;
import kr.co.yournews.infra.mail.MailSenderAdapter;
import kr.co.yournews.infra.mail.strategy.MailStrategyFactory;
import kr.co.yournews.infra.mail.type.MailType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PassCodeManager {
    private final UserService userService;
    private final PassCodeService passCodeService;
    private final MailSenderAdapter mailSenderAdapter;
    private final MailStrategyFactory mailStrategyFactory;
    private final PasswordEncodeService passwordEncodeService;

    /**
     * 사용자 정보를 검증하고 비밀번호 재설정 링크를 전송
     *
     * @param verifyUserDto : 사용자 검증 요청 (username, email)
     * @throws CustomException INVALID_USER_INFO : 일치하는 사용자가 존재하지 않을 경우
     */
    @Transactional(readOnly = true)
    public void initiatePasswordReset(PassResetDto.VerifyUser verifyUserDto) {
        log.info("[비밀번호 재설정 요청] username={}, email={}", verifyUserDto.username(), verifyUserDto.email());

        if (!userService.existsByUsernameAndEmail(verifyUserDto.username(), verifyUserDto.email())) {
            throw new CustomException(UserErrorType.INVALID_USER_INFO);
        }
        String uuid = passCodeService.generateResetUuidAndStore(verifyUserDto.username());
        mailSenderAdapter.sendMail(
                verifyUserDto.email(),
                uuid,
                mailStrategyFactory.getStrategy(MailType.PASS)
        );

        log.info("[비밀번호 재설정 메일 발송 완료] username={}, email={}, uuid={}", verifyUserDto.username(), verifyUserDto.email(), uuid);
    }

    /**
     * 비밀번호 재설정 링크를 통해 받은 UUID를 검증한 뒤, 비밀번호를 실제로 변경
     *
     * @param resetPasswordDto : 재설정 요청 (username, uuid, new password)
     * @throws CustomException UNAUTHORIZED_ACTION : 인증되지 않은 uuid일 경우
     */
    @Transactional
    public void applyNewPassword(PassResetDto.ResetPassword resetPasswordDto) {
        log.info("[비밀번호 재설정 변경 시도] username={}, uuid={}", resetPasswordDto.username(), resetPasswordDto.uuid());

        passCodeService.validateResetUuid(resetPasswordDto.username(), resetPasswordDto.uuid());

        User user = userService.readByUsername(resetPasswordDto.username())
                .orElseThrow(() -> new CustomException(UserErrorType.NOT_FOUND));

        user.updatePassword(passwordEncodeService.encode(resetPasswordDto.password()));

        log.info("[비밀번호 재설정 변경 완료] userId={}", user.getId());
    }
}
