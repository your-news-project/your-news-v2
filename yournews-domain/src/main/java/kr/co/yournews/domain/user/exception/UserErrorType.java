package kr.co.yournews.domain.user.exception;

import kr.co.yournews.common.response.StatusCode;
import kr.co.yournews.common.response.error.type.BaseErrorType;
import lombok.RequiredArgsConstructor;

/**
 * User ErrorCode: Uxxx
 */
@RequiredArgsConstructor
public enum UserErrorType implements BaseErrorType {

    NOT_FOUND(StatusCode.NOT_FOUND, "U001", "존재하지 않는 사용자입니다."),
    NOT_MATCHED_PASSWORD(StatusCode.BAD_REQUEST, "U002", "비밀번호가 일치하지 않습니다."),
    ALREADY_MAIL_REQUEST(StatusCode.TOO_MANY_REQUESTS, "U003", "1분 후 재전송 해주세요."),
    EXIST_EMAIL(StatusCode.CONFLICT, "U004", "이미 존재하는 이메일입니다."),
    CODE_EXPIRED(StatusCode.GONE, "U005", "유효시간이 지났습니다."),
    INVALID_CODE(StatusCode.BAD_REQUEST, "U006", "인증번호가 일치하지 않습니다."),
    INVALID_USER_INFO(StatusCode.BAD_REQUEST, "U007", "정보를 정확히 입력해주세요."),
    CODE_NOT_VERIFIED(StatusCode.FORBIDDEN, "U009", "이메일 인증을 완료해주세요."),
    EXIST_NICKNAME(StatusCode.CONFLICT, "U010", "이미 존재하는 닉네임입니다."),
    DEACTIVATED(StatusCode.FORBIDDEN, "U011", "탈퇴한 사용자입니다."),
    ALREADY_ACTIVE(StatusCode.CONFLICT, "U012", "이미 활성화된 사용자입니다."),
    NOT_ADMIN(StatusCode.FORBIDDEN, "U013", "관리자 권한이 없습니다."),
    BANNED(StatusCode.FORBIDDEN, "U014", "정지된 사용자입니다. 관리자에게 문의해주세요."),
    INVALID_PASSWORD_RESET_REQUEST(StatusCode.BAD_REQUEST, "U015", "아이디 또는 인증 정보가 올바르지 않습니다."),
    EXPIRED_PASSWORD_RESET_CODE(StatusCode.BAD_REQUEST, "U016", "비밀번호 재설정 링크가 만료되었습니다. 다시 요청해주세요."),
    ;

    private final StatusCode status;
    private final String code;
    private final String message;

    @Override
    public StatusCode getStatus() {
        return status;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
