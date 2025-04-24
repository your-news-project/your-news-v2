package kr.co.yournews.infra.fcm.exception;

public class FcmSendFailureException extends RuntimeException {
    public FcmSendFailureException(String message) {
        super(message);
    }
}
