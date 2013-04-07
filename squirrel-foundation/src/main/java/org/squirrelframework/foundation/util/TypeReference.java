package org.squirrelframework.foundation.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 
 * @see <a href="http://gafter.blogspot.com/2006/12/super-type-tokens.html"> Super Type Tokens </a>
 * 
 * @author Henry He
 *
 * @param <T> Generic Type
 */
public abstract class TypeReference<T> {

    private final Type type;

    protected TypeReference() {
        Type superClass = getClass().getGenericSuperclass();

        type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
    }

    public Type getType() {
        return type;
    }
}
