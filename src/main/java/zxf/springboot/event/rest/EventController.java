package zxf.springboot.event.rest;

import com.google.common.eventbus.EventBus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import zxf.springboot.event.model.GuavaEvent;
import zxf.springboot.event.model.SpringEvent;
import zxf.springboot.event.model.UnhandledEvent;
import zxf.springboot.event.service.OrderService;

import java.util.Map;

/**
 * Endpoints that trigger each event mechanism so the dispatch behaviour can be observed from
 * the logs (thread name + start/end ordering).
 *
 * <ul>
 *   <li>Spring: {@code /event/spring} ({@code @EventListener}, async) and
 *       {@code /event/order} ({@code @TransactionalEventListener}).</li>
 *   <li>Guava: {@code /event/guava/sync}, {@code /event/guava/async}, and
 *       {@code /event/guava/dead} (DeadEvent).</li>
 * </ul>
 */
@Slf4j
@RestController
@RequestMapping("/event")
@RequiredArgsConstructor
public class EventController {

    private final ApplicationEventPublisher eventPublisher;
    private final OrderService orderService;
    @Qualifier("syncEventBus")
    private final EventBus syncEventBus;
    @Qualifier("asyncEventBus")
    private final EventBus asyncEventBus;

    @GetMapping("/spring")
    public Map<String, Object> spring(@RequestParam String data) {
        log.info("Request /event/spring, data: {}", data);
        eventPublisher.publishEvent(new SpringEvent(data));
        return Map.of("mechanism", "spring", "data", data, "async", true);
    }

    @GetMapping("/guava/sync")
    public Map<String, Object> guavaSync(@RequestParam String data) {
        log.info("Request /event/guava/sync, data: {}", data);
        syncEventBus.post(new GuavaEvent(data));
        return Map.of("mechanism", "guava", "data", data, "async", false);
    }

    @GetMapping("/guava/async")
    public Map<String, Object> guavaAsync(@RequestParam String data) {
        log.info("Request /event/guava/async, data: {}", data);
        asyncEventBus.post(new GuavaEvent(data));
        return Map.of("mechanism", "guava", "data", data, "async", true);
    }

    @GetMapping("/guava/dead")
    public Map<String, Object> guavaDead(@RequestParam String data) {
        log.info("Request /event/guava/dead, data: {}", data);
        syncEventBus.post(new UnhandledEvent(data)); // no subscriber for UnhandledEvent -> DeadEvent
        return Map.of("mechanism", "guava", "data", data, "dead", true);
    }

    @GetMapping("/order")
    public Map<String, Object> order(@RequestParam(defaultValue = "false") boolean fail,
                                     @RequestParam String data) {
        log.info("Request /event/order, fail: {}, data: {}", fail, data);
        if (fail) {
            try {
                orderService.createAndRollback(data); // throws -> transaction rolls back
            } catch (IllegalStateException ex) {
                log.info("Order rolled back as requested: {}", ex.getMessage());
            }
            return Map.of("mechanism", "spring-transactional", "data", data, "rolledBack", true);
        }
        String orderId = orderService.create(data);
        return Map.of("mechanism", "spring-transactional", "orderId", orderId, "rolledBack", false);
    }
}
