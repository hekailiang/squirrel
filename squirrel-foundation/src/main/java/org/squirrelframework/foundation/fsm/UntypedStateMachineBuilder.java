package org.squirrelframework.foundation.fsm;

public interface UntypedStateMachineBuilder extends StateMachineBuilder<UntypedStateMachine, Object, Object, Object> {
    
    @Deprecated
    <T extends UntypedStateMachine> T newUntypedStateMachine(Object initialStateId, Class<T> stateMachineImplClazz);
    
    <T extends UntypedStateMachine> T newUntypedStateMachine(Object initialStateId);
}
