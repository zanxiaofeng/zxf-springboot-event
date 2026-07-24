package zxf.springboot.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import zxf.springboot.event.model.OrderCreatedEvent;

/**
 * Publishes {@link OrderCreatedEvent} inside a transaction so that
 * {@code @TransactionalEventListener} methods fire at the correct phase.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public String create(String data) {
        Assert.hasText(data, "data must not be blank");
        String orderId = "ORD-" + System.currentTimeMillis();
        log.info("Creating order, orderId: {}, data: {}", orderId, data);
        eventPublisher.publishEvent(new OrderCreatedEvent(orderId, data));
        return orderId;
    }

    @Transactional
    public String createAndRollback(String data) {
        Assert.hasText(data, "data must not be blank");
        String orderId = "ORD-" + System.currentTimeMillis();
        log.info("Creating order then rolling back, orderId: {}", orderId);
        eventPublisher.publishEvent(new OrderCreatedEvent(orderId, data));
        throw new IllegalStateException("intentional rollback to demonstrate AFTER_ROLLBACK");
    }
}
