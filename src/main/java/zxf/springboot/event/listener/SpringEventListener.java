package zxf.springboot.event.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import zxf.springboot.event.model.SpringEvent;

@Slf4j
@Component
public class SpringEventListener {
    @Async
    @EventListener
    public void onEvent(SpringEvent springEvent) throws InterruptedException {
        log.info("::onEvent.start, " + springEvent.getData());
        Thread.sleep(500);
        log.info("::onEvent.end, " + springEvent.getData());
    }
}
