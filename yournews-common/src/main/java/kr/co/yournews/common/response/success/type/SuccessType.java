package kr.co.yournews.common.response.success.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SuccessType {

    ;

    private final HttpStatus status;
    private final String message;

    public int getStatusCode(){
        return status.value();
    }
}
