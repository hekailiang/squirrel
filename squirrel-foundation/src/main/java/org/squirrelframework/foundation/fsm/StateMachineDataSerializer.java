package org.squirrelframework.foundation.fsm;

public interface StateMachineDataSerializer<T extends StateMachine<T, S, E, C>, S, E, C> {
    
    String serialize(StateMachineData.Reader<T, S, E, C> data);
    
    StateMachineData.Reader<T, S, E, C> deserialize(String value);
}
