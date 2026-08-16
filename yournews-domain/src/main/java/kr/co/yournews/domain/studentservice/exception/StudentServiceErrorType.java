package kr.co.yournews.domain.studentservice.exception;

import kr.co.yournews.common.response.StatusCode;
import kr.co.yournews.common.response.error.type.BaseErrorType;
import lombok.RequiredArgsConstructor;

/**
 * StudentService ErrorCode: SSxxx
 */
@RequiredArgsConstructor
public enum StudentServiceErrorType implements BaseErrorType {

    NOT_FOUND(StatusCode.NOT_FOUND, "SS001", "존재하지 않는 홍보 게시물입니다."),
    FORBIDDEN(StatusCode.FORBIDDEN, "SS002", "접근 권한이 없습니다."),
    ALREADY_LIKED(StatusCode.CONFLICT, "SS003", "이미 좋아요를 누른 홍보 게시물입니다."),
    TOO_MANY_IMAGES(StatusCode.BAD_REQUEST, "SS004", "이미지는 최대 3장까지 등록할 수 있습니다."),
    INVALID_IMAGE_FORMAT(StatusCode.BAD_REQUEST, "SS005", "JPG, PNG, WEBP 이미지만 등록할 수 있습니다."),
    IMAGE_TOO_LARGE(StatusCode.BAD_REQUEST, "SS006", "이미지는 장당 최대 5MB까지 등록할 수 있습니다."),
    IMAGE_UPLOAD_FAILED(StatusCode.INTERNAL_SERVER_ERROR, "SS007", "이미지 업로드에 실패했습니다."),
    NOT_EDITABLE(StatusCode.CONFLICT, "SS008", "승인 대기 중인 홍보 게시물만 수정할 수 있습니다."),
    INVALID_IMAGE_SELECTION(StatusCode.BAD_REQUEST, "SS009", "기존 홍보 게시물에 등록되지 않은 이미지 URL입니다."),
    NOT_PROMOTABLE(StatusCode.CONFLICT, "SS010", "승인된 내 홍보 게시물만 메인에 띄울 수 있습니다."),
    PROMOTION_LIMIT_REACHED(StatusCode.CONFLICT, "SS011", "한 캠페인은 최대 8회, 24시간까지 홍보할 수 있습니다.")
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
