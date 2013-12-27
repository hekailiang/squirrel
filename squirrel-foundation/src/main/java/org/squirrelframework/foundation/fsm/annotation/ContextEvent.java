package org.squirrelframework.foundation.fsm.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target({TYPE})
@Documented
public @interface ContextEvent {
    /**
     * @return event name of state machine started
     */
    String startEvent()     default "";
    /**
     * @return event name of parallel transition finished
     */
    String finishEvent()    default "";
    /**
     * @return event name of state machine terminated
     */
    String terminateEvent() default "";
}
