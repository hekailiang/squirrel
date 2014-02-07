package org.squirrelframework.foundation.fsm;

public interface UntypedStateMachineBuilder extends StateMachineBuilder<UntypedStateMachine, Object, Object, Object> {
    
    @Deprecated
    <T extends UntypedStateMachine> T newUntypedStateMachine(Object initialStateId, Class<T> stateMachineImplClazz);
    
    <T extends UntypedStateMachine> T newUntypedStateMachine(Object initialStateId);
    
//    <T extends UntypedStateMachine> T newUntypedStateMachine(Object initialStateId, Object... extraParams);
    
    <T> T newAnyStateMachine(Object initialStateId);
            
//    <T> T newAnyStateMachine(Object initialStateId, Object... extraParams);
}
