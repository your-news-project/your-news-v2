package kr.co.yournews.apis.user.scheduler;

import kr.co.yournews.apis.user.service.UserCleanupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserScheduler {
    private final UserCleanupService userCleanupService;

    @Scheduled(cron = "0 0 1 * * *")
    public void cleanUpSoftDeletedUsersJob() {
        log.info("[사용자 정리 스케줄 시작] soft delete 대상 사용자 정리");
        userCleanupService.deleteSoftDeletedUsersBefore();
    }
}
