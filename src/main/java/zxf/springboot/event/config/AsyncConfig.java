package zxf.springboot.event.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.Arrays;
import java.util.concurrent.Executor;

/**
 * Configures execution of {@code @Async} methods (used by Spring {@code @EventListener}s).
 *
 * <ul>
 *   <li><b>Virtual-thread executor</b> (Java 21) — each async task runs on its own virtual
 *       thread via {@link SimpleAsyncTaskExecutor#setVirtualThreads(boolean)}.</li>
 *   <li><b>Custom {@link AsyncUncaughtExceptionHandler}</b> — without it, exceptions thrown
 *       by {@code @Async} methods are only logged-and-swallowed by Spring's default handler;
 *       here they are logged with the method and arguments.</li>
 * </ul>
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    @Override
    public Executor getAsyncExecutor() {
        var executor = new SimpleAsyncTaskExecutor("spring-async-");
        executor.setVirtualThreads(true);
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> log.error(
                "Unhandled @Async exception in {}.{}(), params: {}",
                method.getDeclaringClass().getSimpleName(),
                method.getName(),
                Arrays.toString(params),
                ex);
    }
}
