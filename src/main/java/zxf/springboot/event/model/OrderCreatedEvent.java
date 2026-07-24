package zxf.springboot.event.model;

/**
 * Spring event published <em>inside</em> a transaction by {@code OrderService}, observed by
 * {@code @TransactionalEventListener} methods that fire at specific transaction phases.
 */
public record OrderCreatedEvent(String orderId, String data) {
}
