package org.squirrelframework.foundation.fsm;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.squirrelframework.foundation.fsm.annotation.State;
import org.squirrelframework.foundation.fsm.annotation.States;
import org.squirrelframework.foundation.fsm.annotation.Transit;
import org.squirrelframework.foundation.fsm.annotation.Transitions;
import org.squirrelframework.foundation.fsm.builder.StateMachineBuilder;
import org.squirrelframework.foundation.fsm.impl.AbstractStateMachine;
import org.squirrelframework.foundation.fsm.impl.StateMachineBuilderImpl;

public class ParallelStateMachineTest {
	
	enum PState {
		Total, A, A1, A1a, A1b, A2, A2a, A2b, B
	}
	
	enum PEvent {
		A1a2A1b, A1b2A1a, A2a2A2b, A2b2A2a, A2B, B2A
	}
	
	@States({
		@State(name="Total", entryCallMethod="enterTotal", exitCallMethod="exitTotal"),
		@State(parent="Total", name="A", entryCallMethod="enterA", exitCallMethod="exitA", compositeType=StateCompositeType.PARALLEL),
		
		@State(parent="A", name="A1", entryCallMethod="enterA1", exitCallMethod="exitA1"),
		@State(parent="A1", name="A1a", entryCallMethod="enterA1a", exitCallMethod="exitA1a", initialState=true),
		@State(parent="A1", name="A1b", entryCallMethod="enterA1b", exitCallMethod="exitA1b"),
		
		@State(parent="A", name="A2", entryCallMethod="enterA2", exitCallMethod="exitA2"),
		@State(parent="A2", name="A2a", entryCallMethod="enterA2a", exitCallMethod="exitA2a"),
		@State(parent="A2", name="A2b", entryCallMethod="enterA2b", exitCallMethod="exitA2b", initialState=true),
		
		@State(parent="Total", name="B", entryCallMethod="enterB", exitCallMethod="exitB")
	})
	@Transitions({
		@Transit(from="A", to="B", on="A2B", callMethod="transitA2B"),
		@Transit(from="B", to="A", on="B2A", callMethod="transitB2A"),
		@Transit(from="A1a", to="A1b", on="A1a2A1b", callMethod="transitA1a2A1b"),
		@Transit(from="A1b", to="A1a", on="A1b2A1a", callMethod="transitA1b2A1a"),
		@Transit(from="A2a", to="A2b", on="A2a2A2b", callMethod="transitA2a2A2b"),
		@Transit(from="A2b", to="A2a", on="A2b2A2a", callMethod="transitA2b2A2a"),
	})
	static class ParallelStateMachine extends AbstractStateMachine<ParallelStateMachine, PState, PEvent, Integer> {
		private StringBuilder logger = new StringBuilder();

		public ParallelStateMachine(ImmutableState<ParallelStateMachine, PState, PEvent, Integer> initialState,
                Map<PState, ImmutableState<ParallelStateMachine, PState, PEvent, Integer>> states) {
	        super(initialState, states);
        }
		
		public void transitA2a2A2b(PState from, PState to, PEvent event, Integer context) {
			addOptionalDot();
			logger.append("transitA2a2A2b");
		}
		
		public void transitA2b2A2a(PState from, PState to, PEvent event, Integer context) {
			addOptionalDot();
			logger.append("transitA2b2A2a");
		}
		
		public void transitA1b2A1a(PState from, PState to, PEvent event, Integer context) {
			addOptionalDot();
			logger.append("transitA1b2A1a");
		}
		
		public void transitA1a2A1b(PState from, PState to, PEvent event, Integer context) {
			addOptionalDot();
			logger.append("transitA1a2A1b");
		}
		
		public void transitB2A(PState from, PState to, PEvent event, Integer context) {
			addOptionalDot();
			logger.append("transitB2A");
		}
		
		public void transitA2B(PState from, PState to, PEvent event, Integer context) {
			addOptionalDot();
			logger.append("transitA2B");
		}
		
		public void enterTotal(PState from, PState to, PEvent event, Integer context) {
			addOptionalDot();
			logger.append("enterTotal");
		}
		
		public void exitTotal(PState from, PState to, PEvent event, Integer context) {
			addOptionalDot();
			logger.append("exitTotal");
		}
		
		public void enterA(PState from, PState to, PEvent event, Integer context) {
			addOptionalDot();
			logger.append("enterA");
		}
		
		public void exitA(PState from, PState to, PEvent event, Integer context) {
			addOptionalDot();
			logger.append("exitA");
		}
		
		public void enterA1(PState from, PState to, PEvent event, Integer context) {
			addOptionalDot();
			logger.append("enterA1");
		}
		
		public void exitA1(PState from, PState to, PEvent event, Integer context) {
			addOptionalDot();
			logger.append("exitA1");
		}
		
		public void enterA1a(PState from, PState to, PEvent event, Integer context) {
			addOptionalDot();
			logger.append("enterA1a");
		}
		
		public void exitA1a(PState from, PState to, PEvent event, Integer context) {
			addOptionalDot();
			logger.append("exitA1a");
		}
		
		public void enterA1b(PState from, PState to, PEvent event, Integer context) {
			addOptionalDot();
			logger.append("enterA1b");
		}
		
		public void exitA1b(PState from, PState to, PEvent event, Integer context) {
			addOptionalDot();
			logger.append("exitA1b");
		}
		
		public void enterA2(PState from, PState to, PEvent event, Integer context) {
			addOptionalDot();
			logger.append("enterA2");
		}
		
		public void exitA2(PState from, PState to, PEvent event, Integer context) {
			addOptionalDot();
			logger.append("exitA2");
		}
		
		public void enterA2a(PState from, PState to, PEvent event, Integer context) {
			addOptionalDot();
			logger.append("enterA2a");
		}
		
		public void exitA2a(PState from, PState to, PEvent event, Integer context) {
			addOptionalDot();
			logger.append("exitA2a");
		}
		
		public void enterA2b(PState from, PState to, PEvent event, Integer context) {
			addOptionalDot();
			logger.append("enterA2b");
		}
		
		public void exitA2b(PState from, PState to, PEvent event, Integer context) {
			addOptionalDot();
			logger.append("exitA2b");
		}
		
		public void enterB(PState from, PState to, PEvent event, Integer context) {
			addOptionalDot();
			logger.append("enterB");
		}
		
		public void exitB(PState from, PState to, PEvent event, Integer context) {
			addOptionalDot();
			logger.append("exitB");
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
	
	private ParallelStateMachine stateMachine;
	
	@BeforeClass
	public static void beforeTest() {
		ConverterProvider.INSTANCE.register(PEvent.class, new Converter.EnumConverter<PEvent>(PEvent.class));
        ConverterProvider.INSTANCE.register(PState.class, new Converter.EnumConverter<PState>(PState.class));
	}
	
	@After
	public void teardown() {
		stateMachine.terminate(null);
	}
	
	@Before
    public void setup() {
		StateMachineBuilder<ParallelStateMachine, PState, PEvent, Integer> builder = StateMachineBuilderImpl.
				newStateMachineBuilder(ParallelStateMachine.class, PState.class, PEvent.class, Integer.class);
		stateMachine = builder.newStateMachine(PState.A);
	}
	
	@Test
	public void testInitialParallelStates() {
		stateMachine.start(null);
		assertThat(stateMachine.consumeLog(), is(equalTo("enterTotal.enterA.enterA1.enterA1a.enterA2.enterA2b")));
		assertThat(stateMachine.getCurrentState(), is(equalTo(PState.A)));
		assertThat(stateMachine.getSubStatesOn(PState.A), contains(PState.A1a, PState.A2b));
	}
	
	@Test
	public void testReceiveSubStateEvent() {
		stateMachine.start(null);
		stateMachine.consumeLog();
		stateMachine.fire(PEvent.A1a2A1b, 1);
		assertThat(stateMachine.consumeLog(), is(equalTo("exitA1a.transitA1a2A1b.enterA1b")));
		assertThat(stateMachine.getCurrentState(), is(equalTo(PState.A)));
		assertThat(stateMachine.getSubStatesOn(PState.A), contains(PState.A1b, PState.A2b));
		
		stateMachine.terminate(null);
		assertThat(stateMachine.consumeLog(), is(equalTo("exitA1b.exitA2b.exitA1.exitA2.exitA.exitTotal")));
	}

}
