package org.squirrelframework.foundation.util;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Orders classes by assignability, with closest types ordered first.
 * 
 * @author Henry.He
 *
 * @param <T> type of object to compare
 */
public class ClassComparator<T> implements Comparator<T>, Serializable {

    private static final long serialVersionUID = 7351750287378530968L;

    @Override
    public int compare(T o1, T o2) {
        Class<?> c1 = o1.getClass();
        Class<?> c2 = o2.getClass();
        if (c1.equals(c2)) {
            return 0;
        }
        if (c1.isAssignableFrom(c2)) {
            return -1;
        } else {
            if (!c2.isAssignableFrom(c2)) {
                throw new IllegalArgumentException("The classes share no relation");
            }
            return 1;
        }
    }
    
    public boolean isComparable(final Class<?> c1, final Class<?> c2) {
        return (c1.isAssignableFrom(c2) || c2.isAssignableFrom(c1));
    }
}
