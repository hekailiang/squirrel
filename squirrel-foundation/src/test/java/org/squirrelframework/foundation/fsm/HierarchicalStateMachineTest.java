package org.squirrelframework.foundation.fsm;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.squirrelframework.foundation.fsm.annotation.State;
import org.squirrelframework.foundation.fsm.annotation.States;
import org.squirrelframework.foundation.fsm.annotation.Transit;
import org.squirrelframework.foundation.fsm.annotation.Transitions;
import org.squirrelframework.foundation.fsm.builder.StateMachineBuilder;
import org.squirrelframework.foundation.fsm.impl.AbstractStateMachine;
import org.squirrelframework.foundation.fsm.impl.SCXMLVisitorImpl;
import org.squirrelframework.foundation.fsm.impl.StateMachineBuilderImpl;

public class HierarchicalStateMachineTest {
	
	public enum HState {
		A, A1, A1a, A1a1, A2, A3, B, B1, B2, B3
	}
	
	public enum HEvent {
		A2B, A12A2, A12A3, A12A1a1, A1a12A1, A32A1, A12B3, B12B2, B22A
	}
	
	@States({
		@State(parent="A", name="A3", entryCallMethod="enterA3", exitCallMethod="leftA3"), 
		@State(parent="B", name="B3", entryCallMethod="enterB3", exitCallMethod="leftB3"),
		@State(parent="A1", name="A1a", entryCallMethod="enterA1a", exitCallMethod="leftA1a"),
		@State(parent="A1a", name="A1a1", entryCallMethod="enterA1a1", exitCallMethod="leftA1a1")
		})
	@Transitions({
		@Transit(from="A1", to="A3", on="A12A3", callMethod="transitA12A3"), 
		@Transit(from="A3", to="A1", on="A32A1", callMethod="transitA32A1"), 
		@Transit(from="A1", to="B3", on="A12B3", callMethod="transitA12B3"), 
		@Transit(from="A1", to="A1a1", on="A12A1a1", callMethod="transitA12A1a1"), 
		@Transit(from="A1a1", to="A1", on="A1a12A1", callMethod="transitA1a12A1"), 
		})
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
		
		public void enterA3(HState from, HState to, HEvent event, Integer context) {
			addOptionalDot();
			logger.append("enterA3");
		}
		
		public void leftA3(HState from, HState to, HEvent event, Integer context) {
			addOptionalDot();
			logger.append("leftA3");
		}
		
		public void transitA12A3(HState from, HState to, HEvent event, Integer context) {
			addOptionalDot();
			logger.append("transitA12A3");
		}
		
		public void transitA32A1(HState from, HState to, HEvent event, Integer context) {
			addOptionalDot();
			logger.append("transitA32A1");
		}
		
		public void enterB3(HState from, HState to, HEvent event, Integer context) {
			addOptionalDot();
			logger.append("enterB3");
		}
		
		public void leftB3(HState from, HState to, HEvent event, Integer context) {
			addOptionalDot();
			logger.append("leftB3");
		}
		
		public void transitA12B3(HState from, HState to, HEvent event, Integer context) {
			addOptionalDot();
			logger.append("transitA12B3");
		}
		
		public void enterA1a(HState from, HState to, HEvent event, Integer context) {
			addOptionalDot();
			logger.append("enterA1a");
		}
		
		public void leftA1a(HState from, HState to, HEvent event, Integer context) {
			addOptionalDot();
			logger.append("leftA1a");
		}
		
		public void enterA1a1(HState from, HState to, HEvent event, Integer context) {
			addOptionalDot();
			logger.append("enterA1a1");
		}
		
		public void leftA1a1(HState from, HState to, HEvent event, Integer context) {
			addOptionalDot();
			logger.append("leftA1a1");
		}
		
		public void transitA12A1a1(HState from, HState to, HEvent event, Integer context) {
			addOptionalDot();
			logger.append("transitA12A1a1");
		}
		
		public void transitA1a12A1(HState from, HState to, HEvent event, Integer context) {
			addOptionalDot();
			logger.append("transitA1a12A1");
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
	
	@BeforeClass
	public static void beforeTest() {
		ConverterProvider.INSTANCE.register(HEvent.class, new Converter.EnumConverter<HEvent>(HEvent.class));
        ConverterProvider.INSTANCE.register(HState.class, new Converter.EnumConverter<HState>(HState.class));
	}
	
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
		
		stateMachine.terminate();
		assertThat(stateMachine.consumeLog(), is(equalTo("exitA1.exitA")));
	}
	
	@Test
	public void testDeclarativeHierarchicalState() {
		stateMachine.fire(HEvent.A12A3, 1);
		assertThat(stateMachine.consumeLog(), is(equalTo("entryA.entryA1.exitA1.transitA12A3.enterA3")));
		assertThat(stateMachine.getCurrentState(), is(equalTo(HState.A3)));
		
		stateMachine.fire(HEvent.A32A1, 1);
		assertThat(stateMachine.consumeLog(), is(equalTo("leftA3.transitA32A1.entryA1")));
		assertThat(stateMachine.getCurrentState(), is(equalTo(HState.A1)));
		
		stateMachine.terminate();
	}
	
	@Test
	public void testTransitionBetweenInnerStates() {
		stateMachine.fire(HEvent.A12B3, 1);
		assertThat(stateMachine.consumeLog(), is(equalTo("entryA.entryA1.exitA1.exitA.transitA12B3.entryB.enterB3")));
		assertThat(stateMachine.getCurrentState(), is(equalTo(HState.B3)));
		stateMachine.terminate();
	}
	
	@Test
	public void testTransitionBetweenParentAndChild() {
		stateMachine.fire(HEvent.A12A1a1, 1);
		assertThat(stateMachine.consumeLog(), is(equalTo("entryA.entryA1.transitA12A1a1.enterA1a.enterA1a1")));
		assertThat(stateMachine.getCurrentState(), is(equalTo(HState.A1a1)));
		
		stateMachine.fire(HEvent.A1a12A1, 1);
		assertThat(stateMachine.consumeLog(), is(equalTo("leftA1a1.leftA1a.exitA1.transitA1a12A1.entryA1")));
		assertThat(stateMachine.getCurrentState(), is(equalTo(HState.A1)));
		
		stateMachine.terminate();
	}
	
	@Test
    public void testExportHierarchicalStateMachine() {
        SCXMLVisitor<HierachicalStateMachine, HState, HEvent, Integer> visitor = 
                new SCXMLVisitorImpl<HierachicalStateMachine, HState, HEvent, Integer>();
        stateMachine.accept(visitor);
        visitor.convertSCXMLFile("HierarchicalStateMachine", true);
    }
}
