package org.squirrelframework.foundation.component.impl;

import java.lang.reflect.Method;

import org.squirrelframework.foundation.component.Observable;
import org.squirrelframework.foundation.component.SquirrelProvider;
import org.squirrelframework.foundation.event.PolymorphismEventDispatcher;
import org.squirrelframework.foundation.event.SquirrelEvent;
import org.squirrelframework.foundation.util.ReflectUtils;

public abstract class AbstractSubject implements Observable {
	
	private boolean notifiable = true;
	
	protected PolymorphismEventDispatcher eventDispatcher;

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
		if (eventDispatcher == null) {
            eventDispatcher = SquirrelProvider.getInstance().newInstance(PolymorphismEventDispatcher.class);
        }
        eventDispatcher.register(eventType, listener, method);
    }

	@Override
    public void addListener(Class<?> eventType, Object listener, String methodName) {
		Method method = ReflectUtils.getFirstMethodOfName(listener.getClass(), methodName);
        addListener(eventType, listener, method);
    }

	@Override
    public void removeListener(Class<?> eventType, Object listener, Method method) {
		if (eventDispatcher != null) {
            eventDispatcher.unregister(eventType, listener, method);
        }
    }
	
	public int getListenerSize() {
        return eventDispatcher.getListenerSize();
    }

	@Override
    public void removeListener(Class<?> eventType, Object listener, String methodName) {
		Method method = ReflectUtils.getFirstMethodOfName(listener.getClass(), methodName);
        removeListener(eventType, listener, method);
    }

	@Override
    public void removeListener(Class<?> eventType, Object listener) {
		if (eventDispatcher != null) {
            eventDispatcher.unregister(eventType, listener);
        }
    }

	@Override
    public void removeAllListeners() {
		if (eventDispatcher != null)
            eventDispatcher.unregisterAll();
    }

	@Override
    public void fireEvent(SquirrelEvent event) {
		if (eventDispatcher != null && isNotifiable()) {
            eventDispatcher.fireEvent(event);
        }
    }
}
