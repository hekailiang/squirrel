/**
 * 
 */
package org.squirrelframework.foundation.fsm;

import java.util.Map;

import org.junit.Test;
import org.squirrelframework.foundation.fsm.annotation.StateMachineParameters;
import org.squirrelframework.foundation.fsm.impl.AbstractUntypedStateMachine;

public class PerformanceTest {

    enum FSMEvent {
        ToA, ToB, ToC, ToD
    }

    @StateMachineParameters(stateType = String.class, eventType = FSMEvent.class, contextType = Integer.class)
    static class StateMachineSample extends AbstractUntypedStateMachine {
        protected StateMachineSample(ImmutableUntypedState initialState, Map<Object, ImmutableUntypedState> states) {
            super(initialState, states);
        }
    }

    @Test(timeout = 10000)
    public void manyTransitions() {
        UntypedStateMachineBuilder builder = StateMachineBuilderFactory.create(StateMachineSample.class);
        Action<UntypedStateMachine, Object, Object, Object> action = new AnonymousAction<UntypedStateMachine, Object, Object, Object>() {
            @Override
            public void execute(Object from, Object to, Object event, Object context, UntypedStateMachine stateMachine) {
                // System.out.println(to);
            }
        };

        builder.externalTransition().from("D").to("A").on(FSMEvent.ToA).perform(action);
        builder.externalTransition().from("A").to("B").on(FSMEvent.ToB).perform(action);
        builder.externalTransition().from("B").to("C").on(FSMEvent.ToC).perform(action);
        builder.externalTransition().from("C").to("D").on(FSMEvent.ToD).perform(action);

        UntypedStateMachine fsm = builder.newStateMachine("D");
        final StateMachinePerformanceMonitor performanceMonitor = 
                new StateMachinePerformanceMonitor(fsm.getClass().getName());
        fsm.addDeclarativeListener(performanceMonitor);

        for (int i = 0; i < 10000; i++) {
            fsm.fire(FSMEvent.ToA, 10);
            fsm.fire(FSMEvent.ToB, 10);
            fsm.fire(FSMEvent.ToC, 10);
            fsm.fire(FSMEvent.ToD, 10);
        }
        fsm.removeDeclarativeListener(performanceMonitor);
        System.out.println(performanceMonitor.getPerfModel());
    }
}
