package kr.co.yournews.infra.mail.util;

public final class MailConstants {

    private MailConstants() { }

    public static final String SENDER = "YOUR-NEWS";
    public static final String CODE_SUBJECT = "[YOUR-NEWS] 인증번호 메일입니다.";
    public static final String CODE_TEXT = "[YOUR-NEWS] <br/> 인증번호 : ";
    public static final String PASS_SUBJECT = "[YOUR-NEWS] 비밀번호를 재설정 해주세요.";
    public static final String PASS_TEXT = "아래의 링크에 들어가 비밀번호를 재설정 해주세요. <br/>";
    public static final String INQUIRY_SUBJECT = "[YOUR-NEWS] 문의 사항입니다.";
    public static final String INQUIRY_TEXT = "문의자 이메일: ";
    public static final String STUDENT_SERVICE_SUBJECT = "[YOUR-NEWS] 학생 서비스 승인 요청입니다.";
    public static final String STUDENT_SERVICE_TEXT = "새로운 학생 서비스가 등록되었습니다.<br/><br/>";

}
