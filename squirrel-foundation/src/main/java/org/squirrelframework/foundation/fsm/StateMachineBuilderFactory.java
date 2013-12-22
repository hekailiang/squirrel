package org.squirrelframework.foundation.fsm;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.squirrelframework.foundation.component.SquirrelProvider;
import org.squirrelframework.foundation.util.TypeReference;

/**
 * State machine builder factory to create the state machine builder. Here we use {@link SquirrelProvider} to create the 
 * builder, so user can register different implementation class of {@link StateMachineBuilder} to instantiate different 
 * builder. And also user can register different type of post processor to post process created builder.
 * 
 * @author Henry.He
 *
 */
public class StateMachineBuilderFactory {
    
    public static <T extends StateMachineWithoutContext<T, S, E>, S, E> StateMachineBuilder<T, S, E, Void> create(
            Class<? extends T> stateMachineClazz, Class<S> stateClazz, Class<E> eventClazz) {
        return create(stateMachineClazz, stateClazz, eventClazz, Void.class, true, new Class<?>[0]);
    }
    
    public static <T extends StateMachine<T, S, E, C>, S, E, C> StateMachineBuilder<T, S, E, C> create(
            Class<? extends T> stateMachineClazz, Class<S> stateClazz, Class<E> eventClazz, Class<C> contextClazz) {
        return create(stateMachineClazz, stateClazz, eventClazz, contextClazz, false, new Class<?>[0]);
    }
    
    public static UntypedStateMachineBuilder create(Class<? extends UntypedStateMachine> stateMachineClazz) {
        return create(stateMachineClazz, new Class[0]);
    }
    
    public static UntypedStateMachineBuilder create(Class<? extends UntypedStateMachine> stateMachineClazz, Class<?>... extraConstParamTypes) {
        final StateMachineBuilder<UntypedStateMachine, Object, Object, Object> builder = 
                create(stateMachineClazz, Object.class, Object.class, Object.class, false, extraConstParamTypes);
        return (UntypedStateMachineBuilder) Proxy.newProxyInstance(
                UntypedStateMachineBuilder.class.getClassLoader(), 
                new Class[]{UntypedStateMachineBuilder.class}, 
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args)
                            throws Throwable {
                        return method.invoke(builder, args);
                    }
                });
    }
    
    public static <T extends StateMachine<T, S, E, C>, S, E, C> StateMachineBuilder<T, S, E, C> create(
            Class<? extends T> stateMachineClazz, Class<S> stateClazz, Class<E> eventClazz, 
            Class<C> contextClazz, Class<?>... extraConstParamTypes) {
        return create(stateMachineClazz, stateClazz, eventClazz, contextClazz, false, extraConstParamTypes);
    }
     
    public static <T extends StateMachine<T, S, E, C>, S, E, C> StateMachineBuilder<T, S, E, C> create(
            Class<? extends T> stateMachineClazz, Class<S> stateClazz, Class<E> eventClazz, 
            Class<C> contextClazz, boolean isContextInsensitive, Class<?>... extraConstParamTypes) {
        return SquirrelProvider.getInstance().newInstance(new TypeReference<StateMachineBuilder<T, S, E, C>>() {}, 
                new Class[] { Class.class, Class.class, Class.class, Class.class, boolean.class, Class[].class }, 
                new Object[] { stateMachineClazz, stateClazz, eventClazz, contextClazz, isContextInsensitive, extraConstParamTypes });
    }
}
