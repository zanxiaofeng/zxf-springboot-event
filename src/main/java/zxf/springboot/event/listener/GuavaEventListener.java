package zxf.springboot.event.listener;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import zxf.springboot.event.model.GuavaEvent;
import zxf.springboot.event.support.Delays;

/**
 * Guava EventBus subscriber, registered on BOTH {@code syncEventBus} and {@code asyncEventBus}
 * (see {@code GuavaEventBusConfig}). Dispatch semantics — synchronous vs asynchronous — depend
 * on which bus the event is posted to, not on this class. Because both buses use the same
 * {@code EventBusExceptionHandler}, a thrown exception is logged by that handler regardless of
 * the bus.
 */
@Slf4j
@Component
public class GuavaEventListener {

    @Subscribe
    public void onGuavaEvent(GuavaEvent event) {
        log.info("GuavaEvent.start, data: {}, thread: {}", event.data(), Thread.currentThread().getName());
        Delays.sleep(500);
        log.info("GuavaEvent.end, data: {}", event.data());
    }

    @Subscribe
    public void onDeadEvent(DeadEvent deadEvent) {
        log.info("DeadEvent, event: {}, source: {}", deadEvent.getEvent(), deadEvent.getSource());
    }
}
