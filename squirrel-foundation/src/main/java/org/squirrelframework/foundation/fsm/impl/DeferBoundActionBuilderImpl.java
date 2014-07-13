package org.squirrelframework.foundation.fsm.impl;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.squirrelframework.foundation.component.SquirrelComponent;
import org.squirrelframework.foundation.fsm.*;
import org.squirrelframework.foundation.fsm.builder.*;

import java.util.Collections;
import java.util.List;

public class DeferBoundActionBuilderImpl<T extends StateMachine<T, S, E, C>, S, E, C>
        implements DeferBoundActionBuilder<T, S, E, C>,
        DeferBoundActionFrom<T, S, E, C>, DeferBoundActionTo<T, S, E, C>,
        On<T, S, E, C>, SquirrelComponent {

    private final List<DeferBoundActionInfo<T, S, E, C>> deferBoundActionInfoList;

    private final ExecutionContext executionContext;

    private S from;

    private S to;

    private DeferBoundActionInfo<T, S, E, C> deferBoundActionInfo;

    private Condition<C> condition;

    DeferBoundActionBuilderImpl(
            List<DeferBoundActionInfo<T, S, E, C>> deferBoundActionInfoList,
            ExecutionContext executionContext) {
        this.deferBoundActionInfoList = deferBoundActionInfoList;
        this.executionContext = executionContext;
    }

    @Override
    public void perform(Action<T, S, E, C> action) {
        if (condition == null) {
            deferBoundActionInfo.setActions(Collections.singletonList(action));
        } else {
            deferBoundActionInfo.setActions(Collections
                    .singletonList(warpConditionalAction(action)));
        }
    }

    @Override
    public void perform(List<? extends Action<T, S, E, C>> actions) {
        if (condition == null) {
            deferBoundActionInfo.setActions(actions);
        } else {
            List<Action<T, S, E, C>> wrapActions = Lists.transform(actions,
                    new Function<Action<T, S, E, C>, Action<T, S, E, C>>() {
                        @Override
                        public Action<T, S, E, C> apply(Action<T, S, E, C> action) {
                            return warpConditionalAction(action);
                        }
                    });
            deferBoundActionInfo.setActions(wrapActions);
        }
    }

    @Override
    public void evalMvel(String expression) {
        Action<T, S, E, C> action = FSM.newMvelAction(expression,
                executionContext);
        perform(action);
    }

    @Override
    public void callMethod(String methodName) {
        Action<T, S, E, C> action = FSM.newMethodCallActionProxy(methodName,
                executionContext);
        perform(action);
    }

    @Override
    public On<T, S, E, C> on(E event) {
        deferBoundActionInfo = new DeferBoundActionInfo<T, S, E, C>(from, to,
                event);
        deferBoundActionInfoList.add(deferBoundActionInfo);
        return this;
    }

    @Override
    public On<T, S, E, C> onAny() {
        deferBoundActionInfo = new DeferBoundActionInfo<T, S, E, C>(from, to,
                null);
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

    @Override
    public When<T, S, E, C> when(Condition<C> condition) {
        this.condition = condition;
        return this;
    }

    @Override
    public When<T, S, E, C> whenMvel(String expression) {
        condition = FSM.newMvelCondition(expression,
                executionContext.getScriptManager());
        return this;
    }

    private Action<T, S, E, C> warpConditionalAction(Action<T, S, E, C> action) {
        return new ActionWrapper<T, S, E, C>(action) {
            @Override
            public void execute(S from, S to, E event, C context, T stateMachine) {
                if (Conditions.isSatified(condition, context)) {
                    super.execute(from, to, event, context, stateMachine);
                }
            }
        };
    }
}
