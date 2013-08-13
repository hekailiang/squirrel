package org.squirrelframework.foundation.fsm.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.squirrelframework.foundation.fsm.Condition;
import org.squirrelframework.foundation.fsm.Conditions;
import org.squirrelframework.foundation.fsm.TransitionPriority;
import org.squirrelframework.foundation.fsm.TransitionType;

@Target({ TYPE })
@Retention(RUNTIME)
public @interface Transit {
    String from();

    String to();
    
    String on();
    
    boolean isTargetFinal() default false;

    @SuppressWarnings("rawtypes")
    Class<? extends Condition> when() default Conditions.Always.class;

    TransitionType type() default TransitionType.EXTERNAL;

    String callMethod() default "";
    
    int priority() default TransitionPriority.NORMAL;
}
