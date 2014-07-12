package org.squirrelframework.foundation.fsm;

/**
 * @author Henry.He
 */
public interface Visitable {
    /**
     * Accepts a {@link #Visitor}.
     *
     * @param visitor the visitor.
     */
    void accept(final Visitor visitor);
}
