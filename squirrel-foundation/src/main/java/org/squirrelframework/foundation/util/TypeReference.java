package org.squirrelframework.foundation.util;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

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
    
    /**
     * Returns the raw type of {@code T}. Formally speaking, if {@code T} is returned by
     * {@link java.lang.reflect.Method#getGenericReturnType}, the raw type is what's returned by
     * {@link java.lang.reflect.Method#getReturnType} of the same method object. Specifically:
     * <ul>
     * <li>If {@code T} is a {@code Class} itself, {@code T} itself is returned.
     * <li>If {@code T} is a {@link ParameterizedType}, the raw type of the parameterized type is
     *     returned.
     * <li>If {@code T} is a {@link GenericArrayType}, the returned type is the corresponding array
     *     class. For example: {@code List<Integer>[] => List[]}.
     * <li>If {@code T} is a type variable or a wildcard type, the raw type of the first upper bound
     *     is returned. For example: {@code <X extends Foo> => Foo}.
     * </ul>
     */
    @SuppressWarnings("unchecked")
	public final Class<T> getRawType() {
		Class<?> rawType = getRawType(type);
		// raw type is |T|
		Class<T> result = (Class<T>) rawType;
		return result;
	}
    
	static Class<?> getRawType(Type type) {
		if (type instanceof Class) {
			return (Class<?>) type;
		} else if (type instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType) type;
			// JDK implementation declares getRawType() to return Class<?>
			return (Class<?>) parameterizedType.getRawType();
		} else if (type instanceof GenericArrayType) {
			GenericArrayType genericArrayType = (GenericArrayType) type;
			return getArrayClass(getRawType(genericArrayType.getGenericComponentType()));
		} else if (type instanceof TypeVariable) {
			// First bound is always the "primary" bound that determines the
			// runtime signature.
			return getRawType(((TypeVariable<?>) type).getBounds()[0]);
		} else if (type instanceof WildcardType) {
			// Wildcard can have one and only one upper bound.
			return getRawType(((WildcardType) type).getUpperBounds()[0]);
		} else {
			throw new AssertionError(type + " unsupported");
		}
	}

	static Class<?> getArrayClass(Class<?> componentType) {
		// TODO(user): This is not the most efficient way to handle generic
		// arrays, but is there another way to extract the array class in a
		// non-hacky way (i.e. using String value class names- "[L...")?
		return Array.newInstance(componentType, 0).getClass();
	}
}
