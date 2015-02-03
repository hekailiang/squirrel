package org.squirrelframework.foundation.issues;

import org.junit.Assert;
import org.junit.Test;
import org.squirrelframework.foundation.fsm.*;
import org.squirrelframework.foundation.fsm.impl.AbstractStateMachine;

/**
 * Created by kailianghe on 2/3/15.
 */
public class Issue39 {

    @Test(expected = RuntimeException.class)
    public void testMvelCannotDiscriminatesTransitionsByConditionName() {
        StateMachineBuilder<AuthFlowStateMachine, String, Object, Context> builder = StateMachineBuilderFactory.create(AuthFlowStateMachine.class, String.class, Object.class, Context.class);
        TestAction action1 = new TestAction();
        TestAction action2 = new TestAction();
        builder.externalTransition().from("initial").to("second").on("Start").whenMvel("3 > 2").perform(action1);
        builder.externalTransition().from("initial").to("second").on("Start").whenMvel("3 < 2").perform(action2);
        AuthFlowStateMachine stateMachine = builder.newStateMachine("initial", StateMachineConfiguration.create().enableDebugMode(true));
    }

    @Test
    public void testMvelDiscriminatesTransitionsByConditionName() {
        StateMachineBuilder<AuthFlowStateMachine, String, Object, Context> builder = StateMachineBuilderFactory.create(AuthFlowStateMachine.class, String.class, Object.class, Context.class);
        TestAction action1 = new TestAction();
        TestAction action2 = new TestAction();
        builder.externalTransition().from("initial").to("second").on("Start").whenMvel("script1:::3 > 2").perform(action1);
        builder.externalTransition().from("initial").to("second").on("Start").whenMvel("script2:::3 < 2").perform(action2);
        AuthFlowStateMachine stateMachine = builder.newStateMachine("initial", StateMachineConfiguration.create().enableDebugMode(true));
        stateMachine.fire("Start", new Context());
        Assert.assertEquals("second", stateMachine.getCurrentState());
    }

    @Test(expected = RuntimeException.class)
    public void testCannotDiscriminatesTransitionsByConditionName() {
        StateMachineBuilder<AuthFlowStateMachine, String, Object, Context> builder = StateMachineBuilderFactory.create(AuthFlowStateMachine.class, String.class, Object.class, Context.class);
        TestAction action1 = new TestAction();
        TestAction action2 = new TestAction();
        builder.externalTransition().from("initial").to("second").on("Start").when(new Condition<Context>() {
            @Override
            public boolean isSatisfied(Context context) {
                return true;
            }
            @Override
            public String name() {
                return "duplicateName";
            }
        }).perform(action1);
        builder.externalTransition().from("initial").to("second").on("Start").when(new Condition<Context>() {
            @Override
            public boolean isSatisfied(Context context) {
                return true;
            }

            @Override
            public String name() {
                return "duplicateName";
            }
        }).perform(action2);
        AuthFlowStateMachine stateMachine = builder.newStateMachine("initial", StateMachineConfiguration.create().enableDebugMode(true));
    }

    @Test
    public void testDiscriminatesTransitionsByConditionName() {
        StateMachineBuilder<AuthFlowStateMachine, String, Object, Context> builder = StateMachineBuilderFactory.create(AuthFlowStateMachine.class, String.class, Object.class, Context.class);
        TestAction action1 = new TestAction();
        TestAction action2 = new TestAction();
        builder.externalTransition().from("initial").to("second").on("Start").when(new Condition<Context>() {
            @Override
            public boolean isSatisfied(Context context) {
                return true;
            }

            @Override
            public String name() {
                return "cond1";
            }
        }).perform(action1);
        builder.externalTransition().from("initial").to("second").on("Start").when(new Condition<Context>() {
            @Override
            public boolean isSatisfied(Context context) {
                return true;
            }

            @Override
            public String name() {
                return "cond2";
            }
        }).perform(action2);
        AuthFlowStateMachine stateMachine = builder.newStateMachine("initial", StateMachineConfiguration.create().enableDebugMode(true));
    }
}

class TestAction extends AnonymousAction<AuthFlowStateMachine, String, Object, Context> {
    @Override
    public void execute(String arg0, String arg1, Object arg2, Context arg3, AuthFlowStateMachine arg4) {}
}

class Context {
}

class AuthFlowStateMachine extends AbstractStateMachine<AuthFlowStateMachine, String, Object, Context> {
}
