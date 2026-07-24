package zxf.springboot.event.model;

/**
 * Guava EventBus payload. Guava does not serialize events, so {@code Serializable} is neither
 * required nor useful here.
 */
public record GuavaEvent(String data) {
}
