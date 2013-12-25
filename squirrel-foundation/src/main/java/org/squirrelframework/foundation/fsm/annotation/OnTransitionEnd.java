package org.squirrelframework.foundation.fsm.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Transition end listener annotation
 * @author Henry.He
 *
 */
@Target({METHOD})
@Retention(RUNTIME)
public @interface OnTransitionEnd {
    String when() default "";
}
