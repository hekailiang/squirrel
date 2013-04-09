package org.squirrelframework.foundation.fsm.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.squirrelframework.foundation.fsm.EventKind;

@Retention(RUNTIME)
@Target(FIELD)
@Documented
public @interface EventType {
	EventKind value() default EventKind.CUSTOM;
}
