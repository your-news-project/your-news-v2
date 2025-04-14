package kr.co.yournews.common.response.success.type;

import kr.co.yournews.common.response.exception.StatusCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SuccessType {

    ;

    private final StatusCode status;
    private final String message;

    public int getStatusCode(){
        return status.getCode();
    }
}
