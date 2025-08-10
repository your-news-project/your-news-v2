package kr.co.yournews.apis.notification.constant;

public final class NotificationConstant {
    private NotificationConstant() { }

    private static final String NEWS_NOTIFICATION_TITLE = "[%s] 새로운 소식이 도착했습니다!";
    private static final String DAILY_NEWS_NOTIFICATION_TITLE = "[%s] 일간 소식이 도착했습니다!";

    public static String getNewsNotificationTitle(String newsName) {
        return String.format(NEWS_NOTIFICATION_TITLE, newsName);
    }

    public static String getDailyNewsNotificationTitle(String newsName) {
        return String.format(DAILY_NEWS_NOTIFICATION_TITLE, newsName);
    }
}
