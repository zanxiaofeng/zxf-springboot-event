package zxf.springboot.event.config;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import zxf.springboot.event.exception.EventBusExceptionHandler;
import zxf.springboot.event.listener.GuavaEventListener;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Configures two Guava buses to contrast dispatch models:
 *
 * <ul>
 *   <li><b>{@code syncEventBus}</b> — a plain {@link EventBus}; subscribers run on the posting
 *       thread (synchronous).</li>
 *   <li><b>{@code asyncEventBus}</b> — an {@link AsyncEventBus}; subscribers run on a dedicated
 *       pool. This is the idiomatic way to dispatch Guava events asynchronously, and — unlike
 *       {@code @Async} on a {@code @Subscribe} method — keeps subscriber exceptions flowing to
 *       the {@link EventBusExceptionHandler}.</li>
 * </ul>
 *
 * The same {@link GuavaEventListener} is registered on both buses, so dispatch semantics depend
 * solely on which bus an event is posted to.
 */
@Slf4j
@Configuration
public class GuavaEventBusConfig {

    @Bean
    public EventBus syncEventBus(GuavaEventListener listener, EventBusExceptionHandler handler) {
        EventBus eventBus = new EventBus(handler);
        eventBus.register(listener);
        return eventBus;
    }

    @Bean
    public EventBus asyncEventBus(GuavaEventListener listener, EventBusExceptionHandler handler) {
        Executor executor = Executors.newFixedThreadPool(2, namedDaemonFactory("guava-async-"));
        EventBus eventBus = new AsyncEventBus(executor, handler);
        eventBus.register(listener);
        return eventBus;
    }

    private static java.util.concurrent.ThreadFactory namedDaemonFactory(String prefix) {
        AtomicInteger counter = new AtomicInteger();
        return runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName(prefix + counter.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        };
    }
}
