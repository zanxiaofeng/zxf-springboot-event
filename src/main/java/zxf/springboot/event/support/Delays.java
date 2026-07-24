package zxf.springboot.event.support;

import lombok.experimental.UtilityClass;

/**
 * Sleep helper that restores the interrupt flag on {@link InterruptedException}, so listeners
 * never leak an unhandled {@code InterruptedException} up to the dispatcher.
 */
@UtilityClass
public class Delays {

    public void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
