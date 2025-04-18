package kr.co.yournews.domain.user.exception;

import kr.co.yournews.common.response.error.type.BaseErrorType;
import kr.co.yournews.common.response.StatusCode;
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
    UNAUTHORIZED_ACTION(StatusCode.FORBIDDEN, "U008", "권한이 없습니다."),
    CODE_NOT_VERIFIED(StatusCode.FORBIDDEN, "U009", "이메일 인증을 완료해주세요."),
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
