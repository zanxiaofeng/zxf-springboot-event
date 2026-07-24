package zxf.springboot.event.model;

/**
 * An event type with no subscriber. Posting it produces a Guava
 * {@link com.google.common.eventbus.DeadEvent}.
 */
public record UnhandledEvent(String data) {
}
