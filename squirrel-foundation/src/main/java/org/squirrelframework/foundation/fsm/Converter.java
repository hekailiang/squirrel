package org.squirrelframework.foundation.fsm;

import org.squirrelframework.foundation.component.SquirrelComponent;

import com.google.common.base.Preconditions;

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
    
    /**
     * Enumeration converter which can convert Enum object to its name, and also 
     * convert Enum name to Enum object.
     * 
     * @param <T> enum type
     */
    public class EnumConverter<T extends Enum<T>> implements Converter<T> {
        
        private final Class<T> enumType;
        
        public EnumConverter(Class<T> enumType) {
            this.enumType = enumType;
        }

        @Override
        public String convertToString(T obj) {
            Preconditions.checkNotNull(obj);
            return obj.name();
        }

        @Override
        public T convertFromString(String name) {
            try {
                return Enum.valueOf(enumType, name);
            } catch (IllegalArgumentException e) {
                return null;
            }
            
        }
    }
}
