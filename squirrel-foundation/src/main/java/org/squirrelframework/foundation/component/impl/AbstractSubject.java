package org.squirrelframework.foundation.component.impl;

import java.lang.reflect.Method;

import org.squirrelframework.foundation.component.Observable;
import org.squirrelframework.foundation.component.SquirrelProvider;
import org.squirrelframework.foundation.event.EventMediator;
import org.squirrelframework.foundation.event.SquirrelEvent;
import org.squirrelframework.foundation.util.ReflectUtils;

public abstract class AbstractSubject implements Observable {
	
	private boolean notifiable = true;
	
	private EventMediator eventMediator;

	@Override
    public boolean isNotifiable() {
	    return notifiable;
    }

	@Override
    public void setNotifiable(boolean notifiable) {
	    this.notifiable = notifiable;
    }

	@Override
    public void addListener(Class<?> eventType, Object listener, Method method) {
		if (eventMediator == null) {
            eventMediator = SquirrelProvider.getInstance().newInstance(EventMediator.class);
        }
        eventMediator.register(eventType, listener, method);
    }

	@Override
    public void addListener(Class<?> eventType, Object listener, String methodName) {
		Method method = ReflectUtils.getFirstMethodOfName(listener.getClass(), methodName);
        addListener(eventType, listener, method);
    }

	@Override
    public void removeListener(Class<?> eventType, Object listener, Method method) {
		if (eventMediator != null) {
            eventMediator.unregister(eventType, listener, method);
        }
    }

	@Override
    public void removeListener(Class<?> eventType, Object listener, String methodName) {
		Method method = ReflectUtils.getFirstMethodOfName(listener.getClass(), methodName);
        removeListener(eventType, listener, method);
    }

	@Override
    public void removeListener(Class<?> eventType, Object listener) {
		if (eventMediator != null) {
            eventMediator.unregister(eventType, listener);
        }
    }

	@Override
    public void removeAllListeners() {
		if (eventMediator != null)
            eventMediator.unregisterAll();
    }

	@Override
    public void fireEvent(SquirrelEvent event) {
		if (eventMediator != null && isNotifiable()) {
            eventMediator.fireEvent(event);
        }
    }
}
