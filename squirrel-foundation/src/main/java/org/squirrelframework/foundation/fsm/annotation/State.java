package org.squirrelframework.foundation.fsm.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({TYPE})
@Retention(RUNTIME)
public @interface State {
	String parent() default "";
    String name();
    String alias() default "";
    String entryCallMethod() default "";
    String exitCallMethod() default "";
    boolean initialState() default false;
}
