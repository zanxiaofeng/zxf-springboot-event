package zxf.springboot.event.model;

/**
 * Spring ApplicationEvent payload. Since Spring 4.2 an event does not need to extend
 * {@code ApplicationEvent} — any object can be published via {@code ApplicationEventPublisher}.
 */
public record SpringEvent(String data) {
}
