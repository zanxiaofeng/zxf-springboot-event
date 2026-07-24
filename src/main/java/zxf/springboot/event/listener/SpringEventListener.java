package zxf.springboot.event.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import zxf.springboot.event.model.SpringEvent;
import zxf.springboot.event.support.Delays;

/**
 * Handles {@link SpringEvent}. {@code @Async} offloads handling to a virtual thread (see
 * {@code AsyncConfig}); any exception is routed to the configured {@code AsyncUncaughtExceptionHandler}.
 */
@Slf4j
@Component
public class SpringEventListener {

    @Async
    @EventListener
    public void onSpringEvent(SpringEvent event) {
        log.info("SpringEvent.start, data: {}, thread: {}", event.data(), Thread.currentThread().getName());
        Delays.sleep(500);
        log.info("SpringEvent.end, data: {}", event.data());
    }
}
