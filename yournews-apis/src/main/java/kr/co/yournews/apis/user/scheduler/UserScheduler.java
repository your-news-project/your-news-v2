package kr.co.yournews.apis.user.scheduler;

import kr.co.yournews.apis.user.service.UserCleanupService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserScheduler {
    private final UserCleanupService userCleanupService;

    @Scheduled(cron = "0 0 1 * * *")
    public void cleanUpSoftDeletedUsersJob() {
        userCleanupService.deleteSoftDeletedUsersBefore();
    }
}
