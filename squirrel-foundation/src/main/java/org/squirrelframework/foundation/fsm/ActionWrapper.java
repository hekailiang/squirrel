package org.squirrelframework.foundation.fsm;

public class ActionWrapper<T extends StateMachine<T, S, E, C>, S, E, C>
        implements Action<T, S, E, C> {

    private final Action<T, S, E, C> delegator;

    public ActionWrapper(Action<T, S, E, C> delegator) {
        this.delegator = delegator;
    }

    @Override
    public void execute(S from, S to, E event, C context, T stateMachine) {
        delegator.execute(from, to, event, context, stateMachine);
    }

    @Override
    public String name() {
        return delegator.name();
    }

    @Override
    public int weight() {
        return delegator.weight();
    }

    @Override
    public boolean isAsync() {
        return delegator.isAsync();
    }

    @Override
    public long timeout() {
        return delegator.timeout();
    }
}
