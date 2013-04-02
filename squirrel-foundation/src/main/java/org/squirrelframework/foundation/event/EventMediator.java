package org.squirrelframework.foundation.event;

import java.lang.reflect.Method;
import java.util.LinkedHashSet;

import org.squirrelframework.foundation.component.SquirrelComponent;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;

/**
 * Event mediator dispatch event to proper listener according to event type.
 *  
 * @author Henry.He
 *
 */
public class EventMediator implements SquirrelComponent {
    
    private LinkedHashSet<ListenerMethod> listeners = null;
    
    public void register(Class<?> eventType, Object listener, Method method) {
        if (listeners == null) {
            listeners = new LinkedHashSet<ListenerMethod>();
        }
        listeners.add(new ListenerMethod(eventType, listener, method));
    }
    
    public void unregister(final Class<?> eventType, final Object target) {
        if (listeners != null) {
            Iterators.removeIf(listeners.iterator(), new Predicate<ListenerMethod>() {
                @Override
                public boolean apply(ListenerMethod m) {
                    return m.matches(eventType, target);
                }
            });
        }
    }
    
    public void unregister(final Class<?> eventType, final Object target, final Method method) {
        if (listeners != null) {
            ListenerMethod toBeRemove = Iterators.find(listeners.iterator(), new Predicate<ListenerMethod>() {
                @Override
                public boolean apply(ListenerMethod m) {
                    return m.matches(eventType, target, method);
                }
            });
            if(toBeRemove!=null) {
                listeners.remove(toBeRemove);
            }
        }
    }
    
    public void unregisterAll() {
        listeners = null;
    }

    public void fireEvent(Object event) {
        if (listeners == null) return;
        ListenerMethod[] listenerArray = listeners.toArray(new ListenerMethod[listeners.size()]);
        for (int i = 0; i < listenerArray.length; i++) {
            if (listenerArray[i].getEventType().isAssignableFrom(event.getClass())) {
                listenerArray[i].invokeMethod(event);
            }
        }
    }
}
