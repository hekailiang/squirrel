package org.squirrelframework.foundation.issues;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.junit.Test;
import org.squirrelframework.foundation.fsm.StateMachine;
import org.squirrelframework.foundation.fsm.StateMachineBuilder;
import org.squirrelframework.foundation.fsm.StateMachineBuilderFactory;
import org.squirrelframework.foundation.fsm.impl.AbstractStateMachine;

public class Issue5 {
    
    @Test
    public void parallelStatesTest() {
        final Random random = new Random(System.currentTimeMillis());
 
        // setup FSM
        final StateMachineBuilder<FSM, St, Ev, Object> builder =
                StateMachineBuilderFactory.create(FSM.class, St.class, Ev.class, Object.class);
 
        builder.defineParallelStatesOn(St.Root, St.St1_1, St.St2_1);
        builder.externalTransition().from(St.St1_1).to(St.St1_2).on(Ev.Ev1_1);
        builder.externalTransition().from(St.St1_2).to(St.St1_1).on(Ev.Ev1_2);
 
        builder.externalTransition().from(St.St2_1).to(St.St2_2).on(Ev.Ev2_1);
        builder.externalTransition().from(St.St2_2).to(St.St2_1).on(Ev.Ev2_2);
 
        final FSM fsm = builder.newStateMachine(St.Root);
        fsm.addTransitionCompleteListener(new StateMachine.TransitionCompleteListener<FSM, St, Ev, Object>() {
            @Override
            public void transitionComplete(StateMachine.TransitionCompleteEvent<FSM, St, Ev, Object> event) {
                System.err.println("FSM transition completed from " + event.getSourceState().toString() + " to " + event.getTargetState().toString());
            }
        });
 
        fsm.start();
 
        // run random events firing to see how transactions goes
        final List<Ev> possibleEvents = Arrays.asList(Ev.values());
        final int eventsCount = possibleEvents.size();
        for (int i = 0; i < 100; ++i) {
            Ev eventToThrow = possibleEvents.get(random.nextInt(eventsCount));
            System.err.println("FSM will get event " + eventToThrow.toString());
            fsm.fire(eventToThrow, null);
        }
    }
    
    @Test
    public void parallelStatesTestFixed() {
        // setup FSM
        final StateMachineBuilder<FSM, St, Ev, Object> builder =
                StateMachineBuilderFactory.create(FSM.class, St.class, Ev.class, Object.class);
 
        builder.defineParallelStatesOn(St.Root, St.Pr1, St.Pr2);
        
        builder.defineSequentialStatesOn(St.Pr1, St.St1_1, St.St1_2);
        builder.externalTransition().from(St.St1_1).to(St.St1_2).on(Ev.Ev1_1);
        builder.externalTransition().from(St.St1_2).to(St.St1_1).on(Ev.Ev1_2);
 
        builder.defineSequentialStatesOn(St.Pr2, St.St2_1, St.St2_2);
        builder.externalTransition().from(St.St2_1).to(St.St2_2).on(Ev.Ev2_1);
        builder.externalTransition().from(St.St2_2).to(St.St2_1).on(Ev.Ev2_2);
 
        final FSM fsm = builder.newStateMachine(St.Root);
        fsm.addTransitionCompleteListener(new StateMachine.TransitionCompleteListener<FSM, St, Ev, Object>() {
            @Override
            public void transitionComplete(StateMachine.TransitionCompleteEvent<FSM, St, Ev, Object> event) {
                System.err.println("FSM transition completed from " + event.getSourceState().toString() + " to " + event.getTargetState().toString());
            }
        });
 
        fsm.start();
        List<St> subStates = fsm.getSubStatesOn(St.Root);
        assertThat(subStates, containsInAnyOrder(St.St1_1, St.St2_1));
        
        fsm.fire(Ev.Ev1_1, null);
        subStates = fsm.getSubStatesOn(St.Root);
        assertThat(subStates, containsInAnyOrder(St.St1_2, St.St2_1));
        
        fsm.fire(Ev.Ev2_1, null);
        subStates = fsm.getSubStatesOn(St.Root);
        assertThat(subStates, containsInAnyOrder(St.St1_2, St.St2_2));
    }
 
    private static class FSM extends AbstractStateMachine<FSM, St, Ev, Object> {
    }
 
    private static enum St {
        Root,
        Pr1, // parallel region state 1 
        Pr2, // parallel region state 2
        St1_1,
        St1_2,
        St2_1,
        St2_2
    }
 
    private static enum Ev {
        Ev1_1,
        Ev1_2,
        Ev2_1,
        Ev2_2
    }
}
