package org.squirrel.foundation.fsm.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.squirrel.foundation.fsm.Condition;
import org.squirrel.foundation.fsm.Conditions;
import org.squirrel.foundation.fsm.TransitionType;

@Target({ TYPE })
@Retention(RUNTIME)
public @interface Transit {
    String from();

    String to();

    String on();

    @SuppressWarnings("rawtypes")
    Class<? extends Condition> when() default Conditions.Always.class;

    TransitionType type() default TransitionType.EXTERNAL;

    String callMethod() default "";
}
