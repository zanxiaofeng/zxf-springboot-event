package zxf.springboot.event;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import zxf.springboot.event.listener.SpringEventListener;
import zxf.springboot.event.listener.SpringTransactionalListener;
import zxf.springboot.event.model.GuavaEvent;
import zxf.springboot.event.model.UnhandledEvent;
import zxf.springboot.event.service.OrderService;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Integration tests covering each event mechanism: Spring {@code @EventListener} (async),
 * Guava sync/async buses, {@code DeadEvent}, and {@code @TransactionalEventListener} phases.
 *
 * <p>Guava buses capture the listener instance at {@code register()} time, so a Mockito spy on
 * the listener bean would not observe dispatch. Instead, each Guava test registers a throwaway
 * probe subscriber that collects the events it sees.
 */
@SpringBootTest
class EventFlowTest {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private EventBus syncEventBus;

    @Autowired
    private EventBus asyncEventBus;

    @Autowired
    private OrderService orderService;

    @MockitoSpyBean
    private SpringEventListener springEventListener;

    @MockitoSpyBean
    private SpringTransactionalListener transactionalListener;

    @Test
    void springEventListenerIsInvokedAsync() {
        eventPublisher.publishEvent(new zxf.springboot.event.model.SpringEvent("hello"));

        Awaitility.await().atMost(Duration.ofSeconds(2))
                .untilAsserted(() -> verify(springEventListener).onSpringEvent(any()));
    }

    @Test
    void guavaSyncBusDispatchesToSubscriber() {
        GuavaProbe probe = registerProbe(syncEventBus);
        try {
            syncEventBus.post(new GuavaEvent("sync"));
            Awaitility.await().atMost(Duration.ofSeconds(2)).until(() -> !probe.seen.isEmpty());
            assertThat(probe.seen.get(0).data()).isEqualTo("sync");
        } finally {
            syncEventBus.unregister(probe);
        }
    }

    @Test
    void guavaAsyncBusDispatchesToSubscriber() {
        GuavaProbe probe = registerProbe(asyncEventBus);
        try {
            asyncEventBus.post(new GuavaEvent("async"));
            Awaitility.await().atMost(Duration.ofSeconds(2)).until(() -> !probe.seen.isEmpty());
            assertThat(probe.seen.get(0).data()).isEqualTo("async");
        } finally {
            asyncEventBus.unregister(probe);
        }
    }

    @Test
    void eventWithNoSubscriberRaisesDeadEvent() {
        DeadProbe probe = new DeadProbe();
        syncEventBus.register(probe);
        try {
            syncEventBus.post(new UnhandledEvent("ghost"));
            Awaitility.await().atMost(Duration.ofSeconds(2)).until(() -> !probe.seen.isEmpty());
            assertThat(((UnhandledEvent) probe.seen.get(0).getEvent()).data()).isEqualTo("ghost");
        } finally {
            syncEventBus.unregister(probe);
        }
    }

    @Test
    void transactionalListenersFireAfterCommit() {
        orderService.create("ok");

        Awaitility.await().atMost(Duration.ofSeconds(2))
                .untilAsserted(() -> verify(transactionalListener).onAfterCommit(any()));
        verify(transactionalListener, never()).onAfterRollback(any());
    }

    @Test
    void transactionalListenersFireAfterRollback() {
        assertThatThrownBy(() -> orderService.createAndRollback("fail"))
                .isInstanceOf(IllegalStateException.class);

        Awaitility.await().atMost(Duration.ofSeconds(2))
                .untilAsserted(() -> verify(transactionalListener).onAfterRollback(any()));
        verify(transactionalListener, never()).onAfterCommit(any());
    }

    private static GuavaProbe registerProbe(EventBus bus) {
        GuavaProbe probe = new GuavaProbe();
        bus.register(probe);
        return probe;
    }

    static final class GuavaProbe {
        final List<GuavaEvent> seen = new CopyOnWriteArrayList<>();

        @Subscribe
        public void onGuava(GuavaEvent event) {
            seen.add(event);
        }
    }

    static final class DeadProbe {
        final List<DeadEvent> seen = new CopyOnWriteArrayList<>();

        @Subscribe
        public void onDead(DeadEvent event) {
            seen.add(event);
        }
    }
}
