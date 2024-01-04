package zxf.springboot.event.config;

import com.google.common.eventbus.EventBus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import zxf.springboot.event.listener.GuavaEventListener;

@Configuration
public class GuavaEventBus {
    @Autowired
    private GuavaEventListener guavaEventListener;

    @Bean
    public EventBus initialize() {
        EventBus eventBus = new EventBus();
        eventBus.register(guavaEventListener);
        return eventBus;
    }
}
