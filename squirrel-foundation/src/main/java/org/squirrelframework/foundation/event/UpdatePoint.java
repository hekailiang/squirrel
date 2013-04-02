package org.squirrelframework.foundation.event;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({METHOD})
@Retention(RUNTIME)
public @interface UpdatePoint {
    // TODO-hhe: in future I guess here need to add listener filter, so as the other points
}
