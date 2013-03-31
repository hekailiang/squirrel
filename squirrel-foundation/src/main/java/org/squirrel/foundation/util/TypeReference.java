package org.squirrel.foundation.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * @see http://gafter.blogspot.com/2006/12/super-type-tokens.html
 * 
 * @author Henry He
 *
 * @param <T> Generic Type
 */
public abstract class TypeReference<T> {

    private final Type type;

    protected TypeReference(){
        Type superClass = getClass().getGenericSuperclass();

        type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
    }

    public Type getType() {
        return type;
    }
    
    public final static Type LIST_STRING = new TypeReference<List<String>>() {}.getType();
}
