package zxf.springboot.event.exception;

import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Logs Guava EventBus subscriber exceptions via SLF4J (consistent with the rest of the app).
 *
 * <p>Guava invokes this only when <em>it</em> dispatches the subscriber — i.e. for the plain
 * {@code EventBus} and the {@code AsyncEventBus}. A {@code @Async}-on-{@code @Subscribe}
 * subscriber would instead route its exceptions to Spring's {@code AsyncUncaughtExceptionHandler},
 * bypassing this handler entirely; that is why async dispatch uses {@code AsyncEventBus} here.
 */
@Slf4j
@Component
public class EventBusExceptionHandler implements SubscriberExceptionHandler {

    @Override
    public void handleException(Throwable exception, SubscriberExceptionContext context) {
        log.error("Guava subscriber {} threw while handling event {}",
                context.getSubscriber(), context.getEvent(), exception);
    }
}
