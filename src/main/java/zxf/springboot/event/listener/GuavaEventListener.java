package zxf.springboot.event.listener;

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
    public void onEvent(GuavaEvent guavaEvent) throws InterruptedException {
        log.info("::onEvent.start, " + guavaEvent.getData());
        Thread.sleep(500);
        log.info("::onEvent.end, " + guavaEvent.getData());
    }
}
