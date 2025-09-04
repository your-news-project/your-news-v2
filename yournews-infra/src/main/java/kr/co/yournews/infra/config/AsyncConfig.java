package kr.co.yournews.infra.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@EnableAsync
@Configuration
public class AsyncConfig {

    @Bean(name = "notificationExecutor")
    public Executor notificationExecutor() {
        int processors = Runtime.getRuntime().availableProcessors();    // CPU 코어 수

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(processors * 2);        // 코어 수 x 2 (IO 작업)
        executor.setMaxPoolSize(processors * 3);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("notif-");
        executor.initialize();
        return executor;
    }

    @Bean(name = "detailExecutor")
    public Executor detailExecutor() {
        int processors = Runtime.getRuntime().availableProcessors();

        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setCorePoolSize(processors * 2);        // 코어 수 x 2 (IO 작업)
        ex.setMaxPoolSize(processors * 3);
        ex.setQueueCapacity(200);
        ex.setThreadNamePrefix("detail-");
        ex.initialize();
        return ex;
    }
}
