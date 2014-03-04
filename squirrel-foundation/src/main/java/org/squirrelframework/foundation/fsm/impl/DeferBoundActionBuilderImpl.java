package org.squirrelframework.foundation.fsm.impl;

import java.util.Collections;
import java.util.List;

import org.squirrelframework.foundation.component.SquirrelComponent;
import org.squirrelframework.foundation.fsm.Action;
import org.squirrelframework.foundation.fsm.StateMachine;
import org.squirrelframework.foundation.fsm.builder.DeferBoundActionBuilder;
import org.squirrelframework.foundation.fsm.builder.DeferBoundActionFrom;
import org.squirrelframework.foundation.fsm.builder.DeferBoundActionTo;
import org.squirrelframework.foundation.fsm.builder.When;

public class DeferBoundActionBuilderImpl<T extends StateMachine<T, S, E, C>, S, E, C> 
        implements DeferBoundActionBuilder<T, S, E, C>, DeferBoundActionFrom<T, S, E, C>, 
        DeferBoundActionTo<T, S, E, C>, When<T, S, E, C>, SquirrelComponent {
    
    private final List<DeferBoundActionInfo<T, S, E, C>> deferBoundActionInfoList;
    
    private final ExecutionContext executionContext;
    
    private S from;
    
    private S to;
    
    private DeferBoundActionInfo<T, S, E, C> deferBoundActionInfo;
    
    DeferBoundActionBuilderImpl(List<DeferBoundActionInfo<T, S, E, C>> deferBoundActionInfoList, 
            ExecutionContext executionContext) {
        this.deferBoundActionInfoList = deferBoundActionInfoList;
        this.executionContext = executionContext;
    }

    @Override
    public void perform(Action<T, S, E, C> action) {
        deferBoundActionInfo.setActions(Collections.singletonList(action));
    }

    @Override
    public void perform(List<Action<T, S, E, C>> actions) {
        deferBoundActionInfo.setActions(actions);
    }

    @Override
    public void evalMvel(String expression) {
        Action<T, S, E, C> action = FSM.newMvelAction(expression, executionContext);
        deferBoundActionInfo.setActions(Collections.singletonList(action));
    }

    @Override
    public void callMethod(String methodName) {
        Action<T, S, E, C> action = FSM.newMethodCallActionProxy(methodName, executionContext);
        deferBoundActionInfo.setActions(Collections.singletonList(action));
    }

    @Override
    public When<T, S, E, C> on(E event) {
        deferBoundActionInfo = new DeferBoundActionInfo<T, S, E, C>(from, to, event);
        deferBoundActionInfoList.add(deferBoundActionInfo);
        return this;
    }

    @Override
    public When<T, S, E, C> onAny() {
        deferBoundActionInfo = new DeferBoundActionInfo<T, S, E, C>(from, to, null);
        deferBoundActionInfoList.add(deferBoundActionInfo);
        return this;
    }

    @Override
    public DeferBoundActionTo<T, S, E, C> to(S to) {
        this.to = to;
        return this;
    }

    @Override
    public DeferBoundActionTo<T, S, E, C> toAny() {
        return this;
    }

    @Override
    public DeferBoundActionFrom<T, S, E, C> fromAny() {
        return this;
    }

    @Override
    public DeferBoundActionFrom<T, S, E, C> from(S from) {
        this.from = from;
        return this;
    }

}
