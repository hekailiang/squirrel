package org.squirrelframework.foundation.component;

/**
 * Post process object created by {@link SquirrelProvider}
 * 
 * @author Henry.He
 *
 * @param <T> type of object to be processed
 */
public interface SquirrelPostProcessor<T> {
	
	/**
	 * Post process created component
	 * @param component created component
	 */
    void postProcess(T component);
}
