package zxf.springboot.event.rest;

import com.google.common.eventbus.EventBus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import zxf.springboot.event.model.GuavaEvent;
import zxf.springboot.event.model.SpringEvent;

@Slf4j
@RestController
@RequestMapping("/event")
public class EventController {
    @Autowired
    private EventBus eventBus;
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @GetMapping("/guava")
    public void guava(@RequestParam String data) {
        log.info("::guava.start, " + data);
        eventBus.post(new GuavaEvent(data));
        log.info("::guava.end, " + data);
    }

    @GetMapping("/spring")
    public void spring(@RequestParam String data) {
        log.info("::spring.start, " + data);
        eventPublisher.publishEvent(new SpringEvent(data));
        log.info("::spring.end, " + data);
    }
}
