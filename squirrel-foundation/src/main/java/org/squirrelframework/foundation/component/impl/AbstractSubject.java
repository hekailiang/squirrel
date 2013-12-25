package org.squirrelframework.foundation.component.impl;

import java.lang.reflect.Method;

import org.squirrelframework.foundation.component.Observable;
import org.squirrelframework.foundation.component.SquirrelProvider;
import org.squirrelframework.foundation.event.ListenerMethod;
import org.squirrelframework.foundation.event.PolymorphismEventDispatcher;
import org.squirrelframework.foundation.event.SquirrelEvent;
import org.squirrelframework.foundation.util.ReflectUtils;

import com.google.common.base.Predicate;

public abstract class AbstractSubject implements Observable {
	
	private boolean notifiable = true;
	
	private PolymorphismEventDispatcher eventDispatcher;

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
	
	public void removeListener(Predicate<ListenerMethod> predicate) {
	    if (eventDispatcher != null) {
            eventDispatcher.unregister(predicate);
        }
	}

	@Override
    public void removeListener(Class<?> eventType, Object listener, Method method) {
		if (eventDispatcher != null) {
            eventDispatcher.unregister(eventType, listener, method);
        }
    }
	
	@Override
	public int getListenerSize() {
        return eventDispatcher!=null ? eventDispatcher.getListenerSize() : 0;
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
