package org.squirrelframework.foundation.component;

import java.lang.reflect.Method;

import org.squirrelframework.foundation.event.SquirrelEvent;

public interface Observable {
	boolean isNotifiable();
	
	void setNotifiable(boolean notifiable);
	
	void addListener(Class<?> eventType, Object listener, Method method);
	
	void addListener(Class<?> eventType, Object listener, String methodName);
	
	void removeListener(Class<?> eventType, Object listener, Method method);
	
	void removeListener(Class<?> eventType, Object listener, String methodName);
	
	void removeListener(Class<?> eventType, Object listener);
	
	void removeAllListeners();
	
	void fireEvent(SquirrelEvent event);
}
