# zxf-springboot-event

A Spring Boot 4.1 / Java 21 demo comparing **Spring ApplicationEvent** with **Guava EventBus**,
focusing on each mechanism's dispatch model, async behaviour, and exception handling.

## Run

Requires JDK 21. No Maven wrapper — use system `mvn`.

```bash
mvn spring-boot:run      # http://localhost:8080
mvn test                 # EventFlowTest covers every mechanism below
```

## Endpoints

| Endpoint | Mechanism | What to observe in the logs |
|---|---|---|
| `GET /event/spring?data=…` | Spring `@EventListener` (async) | controller returns immediately; listener runs on a `spring-async-*` **virtual thread** |
| `GET /event/order?data=…` | Spring `@TransactionalEventListener` | `BEFORE_COMMIT` then `AFTER_COMMIT` fire when the transaction commits |
| `GET /event/order?fail=true&data=…` | Spring `@TransactionalEventListener` (rollback) | only `AFTER_ROLLBACK` fires; `AFTER_COMMIT` does not |
| `GET /event/guava/sync?data=…` | Guava `EventBus` | controller **blocks** until the subscriber finishes |
| `GET /event/guava/async?data=…` | Guava `AsyncEventBus` | controller returns immediately; subscriber runs on a `guava-async-*` thread |
| `GET /event/guava/dead?data=…` | Guava `DeadEvent` | an event type with no subscriber is wrapped in a `DeadEvent` |

## Key design points

- **Async Guava = `AsyncEventBus`, not `@Async` on `@Subscribe`.** A Guava subscriber registered
  as a Spring bean *can* end up `@Async`-proxied, but then subscriber exceptions are swallowed
  by Spring's `AsyncUncaughtExceptionHandler` and never reach Guava's `SubscriberExceptionHandler`.
  `AsyncEventBus` keeps Guava's exception handling intact (`EventBusExceptionHandler`).
- **`@Async` (Spring side) uses virtual threads** via `AsyncConfig` (Java 21
  `SimpleAsyncTaskExecutor.setVirtualThreads(true)`), plus a custom `AsyncUncaughtExceptionHandler`
  so async listener failures are logged with context instead of silently swallowed.
- **`@TransactionalEventListener`** fires only at a transaction phase and only when a transaction
  is active; `OrderService` publishes inside `@Transactional` so the phase listeners fire.

## Key classes

**Guava EventBus**
- `com.google.common.eventbus.EventBus` — synchronous dispatch
- `com.google.common.eventbus.AsyncEventBus` — asynchronous dispatch on an `Executor`
- `com.google.common.eventbus.Subscribe` (annotation) — marks a subscriber method
- `com.google.common.eventbus.DeadEvent` — an event posted with no matching subscriber
- `com.google.common.eventbus.SubscriberExceptionHandler` — handles subscriber exceptions

**Spring events**
- `org.springframework.context.ApplicationEventPublisher` — publishes an event
- `org.springframework.context.event.EventListener` (annotation) — event handler, fires immediately
- `org.springframework.context.ApplicationListener` — interface alternative to the annotation
- `org.springframework.transaction.event.TransactionalEventListener` — transaction-phase-bound handler
- `org.springframework.transaction.event.TransactionPhase` — `BEFORE_COMMIT` / `AFTER_COMMIT` / `AFTER_ROLLBACK` / `AFTER_COMPLETION`
