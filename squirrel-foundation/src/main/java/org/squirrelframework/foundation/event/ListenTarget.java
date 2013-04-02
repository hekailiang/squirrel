package org.squirrelframework.foundation.event;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({FIELD, METHOD})
@Retention(RUNTIME)
public @interface ListenTarget {
    /**
     * @return Class type of target listener
     */
    Class<?> value();
}
