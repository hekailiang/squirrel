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

        protected void fromAToB(String from, String to, FSMEvent event, Integer context) {
            System.out.println("Transition from '" + from + "' to '" + to + "' on event '" + event + "' with context '" + context
                    + "'.");
        }

        protected void ontoB(String from, String to, FSMEvent event, Integer context) {
            System.out.println("Entry State \'" + to + "\'.");
        }
    }

    @Test(timeout = 10000)
    public void manyTransitions() {
        UntypedStateMachineBuilder builder = StateMachineBuilderFactory.create(StateMachineSample.class);
        builder.externalTransition().from("A").to("B").on(FSMEvent.ToB).callMethod("fromAToB");
        builder.onEntry("B").callMethod("ontoB");

        UntypedStateMachine fsm = builder.newStateMachine("A");
        fsm.fire(FSMEvent.ToB, 10);

        for (int i = 0; i < 10000; i++) {
            fsm.fire(FSMEvent.ToA, 10);
            fsm.fire(FSMEvent.ToB, 10);
            fsm.fire(FSMEvent.ToC, 10);
            fsm.fire(FSMEvent.ToD, 10);
        }
    }
}
