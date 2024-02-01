package zxf.springboot.event.listener;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import zxf.springboot.event.model.GuavaEvent;

@Slf4j
@Component
public class GuavaEventListener {
    @Async
    @Subscribe
    public void onGuavaEvent(GuavaEvent guavaEvent) throws InterruptedException {
        log.info("::onGuavaEvent.start, " + guavaEvent.getData());
        Thread.sleep(500);
        log.info("::onGuavaEvent.end, " + guavaEvent.getData());
    }

    @Async
    @Subscribe
    public void onDeadEvent(DeadEvent deadEvent) throws InterruptedException {
        log.info("::onDeadEvent.start, " + deadEvent);
        Thread.sleep(500);
        log.info("::onDeadEvent.end, " + deadEvent);
    }
}
