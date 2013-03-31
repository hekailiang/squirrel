package org.squirrel.foundation.component;

public interface CompositePostProcessor<T> extends SquirrelPostProcessor<T> {
    void compose(SquirrelPostProcessor<? super T> processor);
    void decompose(SquirrelPostProcessor<? super T> processor);
    void clear();
}
