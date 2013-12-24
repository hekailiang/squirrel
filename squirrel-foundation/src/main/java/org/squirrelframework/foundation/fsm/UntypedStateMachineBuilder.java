package org.squirrelframework.foundation.fsm;

public interface UntypedStateMachineBuilder extends StateMachineBuilder<UntypedStateMachine, Object, Object, Object> {
    
    <T extends UntypedStateMachine> T newUntypedStateMachine(Object initialStateId, Class<T> stateMachineImplClazz);
}
