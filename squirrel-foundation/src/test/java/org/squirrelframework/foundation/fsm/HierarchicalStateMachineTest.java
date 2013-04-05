package org.squirrelframework.foundation.fsm;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.squirrelframework.foundation.fsm.builder.StateMachineBuilder;
import org.squirrelframework.foundation.fsm.impl.AbstractStateMachine;
import org.squirrelframework.foundation.fsm.impl.StateMachineBuilderImpl;

public class HierarchicalStateMachineTest extends AbstractStateMachineTest {
	
	public enum HState {
		A, A1, A2, B, B1, B2
	}
	
	public enum HEvent {
		A2B, A12A2, B12B2, B22A
	}
	
	static class HierachicalStateMachine extends AbstractStateMachine<HierachicalStateMachine, HState, HEvent, Integer> {
		
		private StringBuilder logger = new StringBuilder();

		public HierachicalStateMachine(
                ImmutableState<HierachicalStateMachine, HState, HEvent, Integer> initialState,
                Map<HState, ImmutableState<HierachicalStateMachine, HState, HEvent, Integer>> states,
                HierachicalStateMachine parent, Class<?> type, boolean isLeaf) {
	        super(initialState, states, parent, type, isLeaf);
        }

		@Override
        protected HEvent getInitialEvent() {
	        return null;
        }

		@Override
        protected Integer getInitialContext() {
	        return 0;
        }

		@Override
        protected HEvent getTerminateEvent() {
	        return null;
        }

		@Override
        protected Integer getTerminateContext() {
	        return -1;
        }
		
		public void entryA(HState from, HState to, HEvent event, Integer context) {
			addOptionalDot();
			logger.append("entryA");
		}
		
		public void exitA(HState from, HState to, HEvent event, Integer context) {
			addOptionalDot();
			logger.append("exitA");
		}
		
		public void transitFromAToBOnA2B(HState from, HState to, HEvent event, Integer context) {
			addOptionalDot();
			logger.append("transitFromAToBOnA2B");
		}
		
		public void entryA1(HState from, HState to, HEvent event, Integer context) {
			addOptionalDot();
			logger.append("entryA1");
		}
		
		public void exitA1(HState from, HState to, HEvent event, Integer context) {
			addOptionalDot();
			logger.append("exitA1");
		}
		
		public void transitFromA1ToA2OnA12A2(HState from, HState to, HEvent event, Integer context) {
			addOptionalDot();
			logger.append("transitFromA1ToA2OnA12A2");
		}
		
		public void entryA2(HState from, HState to, HEvent event, Integer context) {
			addOptionalDot();
			logger.append("entryA2");
		}
		
		public void exitA2(HState from, HState to, HEvent event, Integer context) {
			addOptionalDot();
			logger.append("exitA2");
		}
		
		public void entryB(HState from, HState to, HEvent event, Integer context) {
			addOptionalDot();
			logger.append("entryB");
		}
		
		public void exitB(HState from, HState to, HEvent event, Integer context) {
			addOptionalDot();
			logger.append("exitB");
		}
		
		public void entryB1(HState from, HState to, HEvent event, Integer context) {
			addOptionalDot();
			logger.append("entryB1");
		}
		
		public void exitB1(HState from, HState to, HEvent event, Integer context) {
			addOptionalDot();
			logger.append("exitB1");
		}
		
		public void entryB2(HState from, HState to, HEvent event, Integer context) {
			addOptionalDot();
			logger.append("entryB2");
		}
		
		public void exitB2(HState from, HState to, HEvent event, Integer context) {
			addOptionalDot();
			logger.append("exitB2");
		}
		
		public void transitFromB1ToB2OnB12B2(HState from, HState to, HEvent event, Integer context) {
			addOptionalDot();
			logger.append("transitFromB1ToB2OnB12B2");
		}
		
		public void transitFromB2ToAOnB22A(HState from, HState to, HEvent event, Integer context) {
			addOptionalDot();
			logger.append("transitFromB2ToAOnB22A");
		}
		
		private void addOptionalDot() {
			if (logger.length() > 0) {
				logger.append('.');
			}
		}
		
		public String consumeLog() {
			final String result = logger.toString();
			logger = new StringBuilder();
			return result;
		}
		
	}
	
	HierachicalStateMachine stateMachine;
	
	@Before
    public void setup() {
		StateMachineBuilder<HierachicalStateMachine, HState, HEvent, Integer> builder = 
				StateMachineBuilderImpl.newStateMachineBuilder(
						HierachicalStateMachine.class, HState.class, HEvent.class, Integer.class, new Class<?>[0]);
		builder.transition().from(HState.A).to(HState.B).on(HEvent.A2B);
		
		builder.defineHierachyOn(HState.A, HState.A1, HState.A2);
		builder.transition().from(HState.A1).to(HState.A2).on(HEvent.A12A2);
		
		builder.defineHierachyOn(HState.B, HState.B1, HState.B2);
		builder.transition().from(HState.B1).to(HState.B2).on(HEvent.B12B2);
		builder.transition().from(HState.B2).to(HState.A).on(HEvent.B22A);		
		
		stateMachine = builder.newStateMachine(HState.A, null, Object.class, true, new Object[0]);
	}
	
	@Test
	public void testBasicHierarchicalState() {
		stateMachine.start();
		assertThat(stateMachine.consumeLog(), is(equalTo("entryA.entryA1")));
		assertThat(stateMachine.getCurrentState(), is(equalTo(HState.A1)));
		
		stateMachine.fire(HEvent.A12A2, 1);
		assertThat(stateMachine.consumeLog(), is(equalTo("exitA1.transitFromA1ToA2OnA12A2.entryA2")));
		assertThat(stateMachine.getCurrentState(), is(equalTo(HState.A2)));
		
		stateMachine.fire(HEvent.A2B, 1);
		assertThat(stateMachine.consumeLog(), is(equalTo("exitA2.exitA.transitFromAToBOnA2B.entryB.entryB1")));
		assertThat(stateMachine.getCurrentState(), is(equalTo(HState.B1)));
		
		stateMachine.fire(HEvent.B12B2, 1);
		assertThat(stateMachine.consumeLog(), is(equalTo("exitB1.transitFromB1ToB2OnB12B2.entryB2")));
		assertThat(stateMachine.getCurrentState(), is(equalTo(HState.B2)));
		
		stateMachine.fire(HEvent.B22A, 1);
		assertThat(stateMachine.consumeLog(), is(equalTo("exitB2.exitB.transitFromB2ToAOnB22A.entryA.entryA1")));
		assertThat(stateMachine.getCurrentState(), is(equalTo(HState.A1)));
	}
}
