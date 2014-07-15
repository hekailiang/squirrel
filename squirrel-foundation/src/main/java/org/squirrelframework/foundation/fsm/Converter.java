package org.squirrelframework.foundation.fsm;

import org.squirrelframework.foundation.component.SquirrelComponent;

/**
 * Convert object from string to object and object to string either.
 * 
 * @author Henry.He
 *
 * @param <T> type of converted object
 */
public interface Converter<T> extends SquirrelComponent {
    
    /**
     * Convert object to string.
     * @param obj converted object
     * @return string description of object
     */
    String convertToString(T obj);
    
    /**
     * Convert string to object.
     * @param name name of the object
     * @return converted object
     */
    T convertFromString(String name);
}
