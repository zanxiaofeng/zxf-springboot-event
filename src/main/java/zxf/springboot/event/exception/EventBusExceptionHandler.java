package zxf.springboot.event.exception;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EventBusExceptionHandler implements SubscriberExceptionHandler {
    public void handleException(Throwable exception, SubscriberExceptionContext context) {
        Logger logger = logger(context);
        if (logger.isLoggable(Level.SEVERE)) {
            logger.log(Level.SEVERE, message(context), exception);
        }

    }

    private static Logger logger(SubscriberExceptionContext context) {
        return Logger.getLogger(EventBus.class.getName() + "." + context.getEventBus().identifier());
    }

    private static String message(SubscriberExceptionContext context) {
        Method method = context.getSubscriberMethod();
        return "Exception thrown by subscriber method " + method.getName() + '(' + method.getParameterTypes()[0].getName() + ')' + " on subscriber " + context.getSubscriber() + " when dispatching event: " + context.getEvent();
    }
}
