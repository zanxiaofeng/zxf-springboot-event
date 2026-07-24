# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What This Project Is

A Spring Boot 4.1 / Java 21 demo comparing **Spring ApplicationEvent** with **Guava EventBus** —
their dispatch models, async behaviour, exception handling, and transaction-bound events. Each
mechanism is exposed through an HTTP endpoint so behaviour can be observed from the logs.

## Build & Run

No Maven wrapper — use system `mvn`. Requires **JDK 21** (set `JAVA_HOME`, e.g.
`/home/davis/.jdks/ms-21.0.10`).

```bash
mvn spring-boot:run                              # run on :8080
mvn test                                         # EventFlowTest covers every endpoint's behaviour
mvn test -Dtest=EventFlowTest#guavaSyncBusDispatchesToSubscriber   # single test
```

## Endpoints

| Endpoint | Mechanism | Observable behaviour |
|---|---|---|
| `GET /event/spring?data=` | Spring `@EventListener` (`@Async`) | listener on a `spring-async-*` virtual thread; controller returns immediately |
| `GET /event/order?data=` | Spring `@TransactionalEventListener` | `BEFORE_COMMIT` then `AFTER_COMMIT` on commit |
| `GET /event/order?fail=true&data=` | (rollback) | only `AFTER_ROLLBACK` fires |
| `GET /event/guava/sync?data=` | Guava `EventBus` | subscriber runs on the **HTTP thread** (controller blocks) |
| `GET /event/guava/async?data=` | Guava `AsyncEventBus` | subscriber on a `guava-async-*` thread; controller returns immediately |
| `GET /event/guava/dead?data=` | Guava `DeadEvent` | `UnhandledEvent` has no subscriber → `DeadEvent` fires |

## Architecture

Package `zxf.springboot.event` under `src/main/java`:

- **`config/AsyncConfig`** — `@EnableAsync` + `AsyncConfigurer`. Provides a **virtual-thread**
  executor for `@Async` and a custom `AsyncUncaughtExceptionHandler` (Spring's default swallows
  async exceptions).
- **`config/GuavaEventBusConfig`** — defines **two** `EventBus` beans: `syncEventBus` (plain
  `EventBus`) and `asyncEventBus` (`AsyncEventBus` on a 2-thread pool). The same
  `GuavaEventListener` is registered on both; dispatch semantics depend on which bus you post to.
  Both take the `EventBusExceptionHandler`. (Guava's `Dispatcher` is package-private, so only the
  simple public constructors are usable externally — no `(String, handler)` 2-arg form exists.)
- **`rest/EventController`** — the only entry point. Constructor-injects both buses via
  `@Qualifier` (requires `lombok.config`'s `copyableAnnotations` so Lombok propagates `@Qualifier`
  onto the generated constructor params).
- **`listener/`** — `SpringEventListener` (`@EventListener` + `@Async`),
  `SpringTransactionalListener` (`@TransactionalEventListener` across phases),
  `GuavaEventListener` (`@Subscribe` for `GuavaEvent` + `DeadEvent`).
- **`service/OrderService`** — `@Transactional`; publishes `OrderCreatedEvent` so the transactional
  listeners fire. `createAndRollback` throws to demonstrate `AFTER_ROLLBACK`.
- **`model/`** — `record` event payloads: `SpringEvent`, `GuavaEvent`, `UnhandledEvent`,
  `OrderCreatedEvent`.
- **`support/Delays`** — `@UtilityClass` sleep helper that restores the interrupt flag.
- **`exception/EventBusExceptionHandler`** — Guava `SubscriberExceptionHandler`, SLF4J `@Slf4j`,
  registered as a `@Component`.

### Behaviors verified at runtime

- **`@Async` (Spring side)** runs on `spring-async-*` virtual threads; thrown exceptions are
  routed to `AsyncConfig`'s `AsyncUncaughtExceptionHandler`.
- **Guava async uses `AsyncEventBus`** (not `@Async` on `@Subscribe`). This matters: a Guava
  subscriber reached only via Guava's own dispatch keeps subscriber exceptions flowing to
  `EventBusExceptionHandler`. With `@Async`-on-`@Subscribe` (a Spring proxy around the method),
  exceptions would instead be swallowed by Spring's async handler and never reach Guava's.
- **`@TransactionalEventListener`** fires only at its phase and only when a transaction is active
  (`fallbackExecution = false` by default); `OrderService` publishes inside `@Transactional`.
- The transactional demo needs a real `PlatformTransactionManager`, hence `spring-boot-starter-jdbc`
  + H2 in `application.properties`.
