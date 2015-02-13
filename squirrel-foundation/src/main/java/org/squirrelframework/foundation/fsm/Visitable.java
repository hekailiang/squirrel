package org.squirrelframework.foundation.fsm;

/**
 * @author Henry.He
 */
public interface Visitable {
    /**
     * Accepts a {@link org.squirrelframework.foundation.fsm.Visitor}.
     *
     * @param visitor the visitor.
     */
    void accept(final Visitor visitor);
}
