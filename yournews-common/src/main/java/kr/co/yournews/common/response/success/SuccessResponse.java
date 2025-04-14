package kr.co.yournews.common.response.success;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.co.yournews.common.response.exception.StatusCode;
import kr.co.yournews.common.response.success.type.SuccessType;
import lombok.Builder;
import org.springframework.http.HttpStatus;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SuccessResponse<T>(
        int code,
        String message,
        T data
) {
    public static SuccessResponse<?> ok() {
        return SuccessResponse.builder()
                .code(StatusCode.OK.getCode())
                .message("요청이 성공하였습니다.")
                .build();
    }

    public static SuccessResponse<?> from(SuccessType success) {
        return SuccessResponse.builder()
                .code(success.getStatusCode())
                .message(success.getMessage())
                .build();
    }

    public static <T> SuccessResponse<?> from(T data) {
        return SuccessResponse.builder()
                .code(HttpStatus.OK.value())
                .message("요청이 성공하였습니다.")
                .data(data)
                .build();
    }

    public static <T> SuccessResponse<?> of(SuccessType success, T data) {
        return SuccessResponse.builder()
                .code(success.getStatusCode())
                .message(success.getMessage())
                .data(data)
                .build();
    }
}
