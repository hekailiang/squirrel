package org.squirrelframework.foundation.component;

/**
 * Allows for deferred execution of logic, useful when trying to get multiple components to coordinate behavior. 
 * A component may add a command to be executed "{@linkplain #execute() at the end of the heartbeat}". 
 * Also, Heartbeats can be nested.
 */
public interface Heartbeat {
    /**
     * Begins a new Heartbeat. Heartbeats nest. Every call to begin() should be matched by a call to {@link #execute()}.
     */
    void begin();

    /**
     * Executes all commands since the most recent {@link #begin()}.
     */
    void execute();

    /**
     * Adds a new command to the current Heartbeat. The command will be executed by {@link #end()}.
     * 
     * @param command
     *            command to be executed at the end of the heartbeat
     */
    void defer(Runnable command);
}
