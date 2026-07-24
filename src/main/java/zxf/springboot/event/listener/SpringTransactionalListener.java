package zxf.springboot.event.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import zxf.springboot.event.model.OrderCreatedEvent;

/**
 * Transaction-bound listeners for {@link OrderCreatedEvent}.
 *
 * <p>Unlike a regular {@code @EventListener} (which fires the moment an event is published),
 * a {@code @TransactionalEventListener} fires at a specific {@link TransactionPhase} and only
 * when a transaction is active. {@code AFTER_COMMIT} is the common choice: react to a successful
 * commit (e.g. send a notification) without side effects when the transaction rolls back.
 *
 * <p>By default {@code fallbackExecution = false}, so these methods are skipped if no
 * transaction is active.
 */
@Slf4j
@Component
public class SpringTransactionalListener {

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onBeforeCommit(OrderCreatedEvent event) {
        log.info("OrderCreatedEvent BEFORE_COMMIT, orderId: {}", event.orderId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAfterCommit(OrderCreatedEvent event) {
        log.info("OrderCreatedEvent AFTER_COMMIT, orderId: {}", event.orderId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void onAfterRollback(OrderCreatedEvent event) {
        log.info("OrderCreatedEvent AFTER_ROLLBACK, orderId: {}", event.orderId());
    }
}
