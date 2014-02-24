package org.squirrelframework.foundation.fsm.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ListenerOrder {
    int value();
}
