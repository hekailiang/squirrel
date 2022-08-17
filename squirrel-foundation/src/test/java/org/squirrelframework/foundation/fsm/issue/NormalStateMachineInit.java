package org.squirrelframework.foundation.fsm.issue;
import org.junit.Test;
import org.squirrelframework.foundation.fsm.StateMachineBuilder;
import org.squirrelframework.foundation.fsm.StateMachineBuilderFactory;

import java.util.*;


public class NormalStateMachineInit {


    /**
     * 定义终态、相同的event下、使用了mvel表达式且没有命名，在checkConflictTransitions方法中会认为使用了相同的Transition，抛异常
     * @see org.squirrelframework.foundation.fsm.impl.StateImpl#checkConflictTransitions
     * @see org.squirrelframework.foundation.fsm.impl.TransitionImpl#isMatch(S, S, E, int)
     */
    @Test(expected = Exception.class)
    public void initSignUpStateMachine() {

        StateMachineBuilder<SignupStateMachine, SignUpStatusEnum, BusinessEventEnum, Map> builder =
                StateMachineBuilderFactory.create(SignupStateMachine.class, SignUpStatusEnum.class, BusinessEventEnum.class, Map.class,
                        new Class<?>[0]);
        //1、定义报名主子状态关系
        builder.defineState(SignUpStatusEnum.START);
        builder.defineSequentialStatesOn(SignUpStatusEnum.CREATING,
                SignUpStatusEnum.COMMITTED);
        builder.defineSequentialStatesOn(SignUpStatusEnum.VALID,
                SignUpStatusEnum.SUB_VALID);
        builder.defineSequentialStatesOn(SignUpStatusEnum.INVALID,
                SignUpStatusEnum.AUDIT_REJECT_INVALID, SignUpStatusEnum.QUIT_INVALID, SignUpStatusEnum.CAMPAIGN_INVALID);

        // 定义终态
        builder.defineFinalState(SignUpStatusEnum.AUDIT_REJECT_INVALID);

        // 2 定义transaction
        builder.externalTransition().from(SignUpStatusEnum.START).to(SignUpStatusEnum.COMMITTED).on(BusinessEventEnum.EVENT_COMMIT_SIGN_UP);
        builder.externalTransition().from(SignUpStatusEnum.START).to(SignUpStatusEnum.COMMITTED).on(BusinessEventEnum.EVENT_BATCH_SIGN_UP);

        builder.externalTransition().from(SignUpStatusEnum.COMMITTED).to(SignUpStatusEnum.COMMITTED).on(BusinessEventEnum.EVENT_UPDATE_SIGN_UP);

        builder.externalTransition().from(SignUpStatusEnum.COMMITTED).to(SignUpStatusEnum.SUB_VALID).on(BusinessEventEnum.EVENT_AUDIT_SIGN_UP)
                .whenMvel("context.auditResult == 1");
        builder.externalTransition().from(SignUpStatusEnum.COMMITTED).to(SignUpStatusEnum.AUDIT_REJECT_INVALID).on(BusinessEventEnum.EVENT_AUDIT_SIGN_UP)
                .whenMvel("context.auditResult == 2");

        builder.externalTransitions().fromAmong(SignUpStatusEnum.CREATING).to(SignUpStatusEnum.QUIT_INVALID).on(BusinessEventEnum.EVENT_QUIT_SIGN_UP);

        // 初始化状态机对象
        SignupStateMachine signupStateMachine = builder.newStateMachine(SignUpStatusEnum.START);
    }

}

