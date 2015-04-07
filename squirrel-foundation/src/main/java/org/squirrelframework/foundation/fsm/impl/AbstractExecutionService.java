package org.squirrelframework.foundation.fsm.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squirrelframework.foundation.component.SquirrelConfiguration;
import org.squirrelframework.foundation.component.impl.AbstractSubject;
import org.squirrelframework.foundation.exception.ErrorCodes;
import org.squirrelframework.foundation.exception.SquirrelRuntimeException;
import org.squirrelframework.foundation.exception.TransitionException;
import org.squirrelframework.foundation.fsm.Action;
import org.squirrelframework.foundation.fsm.ActionExecutionService;
import org.squirrelframework.foundation.fsm.StateMachine;
import org.squirrelframework.foundation.fsm.StateMachineContext;
import org.squirrelframework.foundation.util.Pair;

import com.google.common.collect.Maps;

public abstract class AbstractExecutionService<T extends StateMachine<T, S, E, C>, S, E, C> 
    extends AbstractSubject implements ActionExecutionService<T, S, E, C> {
    
    private static final Logger logger = LoggerFactory.getLogger(AbstractExecutionService.class);

    protected final LinkedList<Pair<String, List<ActionContext<T, S, E, C>>>> actionBuckets = 
            new LinkedList<Pair<String, List<ActionContext<T, S, E, C>>>>();
    
    protected boolean dummyExecution = false;
    
    private int actionTotalSize = 0;
    
    @Override
    public void begin(String bucketName) {
        List<ActionContext<T, S, E, C>> actionContext = new ArrayList<ActionContext<T, S, E, C>>();
        actionBuckets.add(new Pair<String, List<ActionContext<T, S, E, C>>>(bucketName, actionContext));
    }
    
    @Override
    public void defer(Action<T, S, E, C> action, S from, S to, E event, C context, T stateMachine) {
        checkNotNull(action, "Action parameter cannot be null.");
        List<ActionContext<T, S, E, C>> actions = actionBuckets.peekLast().second();
        checkNotNull(actions, "Action bucket currently is empty. Make sure execution service is began.");
        actions.add(ActionContext.get(action, from, to, event, context, stateMachine, ++actionTotalSize));
    }
    
    private void doExecute(String bucketName, List<ActionContext<T, S, E, C>> bucketActions) {
        checkNotNull(bucketActions, "Action bucket cannot be empty when executing.");
        final Map<ActionContext<T, S, E, C>, Future<?>> futures = Maps.newHashMap();
        for (int i=0, actionSize = bucketActions.size(); i<actionSize; ++i) {
            final ActionContext<T, S, E, C> actionContext = bucketActions.get(i);
            if(actionContext.action.weight()!=Action.IGNORE_WEIGHT) {
                try {
                    fireEvent(BeforeExecActionEventImpl.get(actionContext.position, actionTotalSize, actionContext));
                    if(dummyExecution) continue;
                    if(actionContext.action.isAsync()) {
                        final boolean isTestEvent = StateMachineContext.isTestEvent();
                        final T instance = StateMachineContext.currentInstance();
                        Future<?> future = SquirrelConfiguration.getExecutor().submit(new Runnable() {
                            @Override
                            public void run() {
                                StateMachineContext.set(instance, isTestEvent);
                                try {
                                    actionContext.run();
                                } finally {
                                    StateMachineContext.set(null);
                                }
                            }
                        });
                        // if run background then not add to this list
                        futures.put(actionContext, future);
                    } else {
                        actionContext.run();
                    }
                } catch (Exception e) {
                    logger.error("Error during transition", e);
                    Throwable t = (e instanceof SquirrelRuntimeException) ?
                            ((SquirrelRuntimeException)e).getTargetException() : e;
                    // wrap any exception into transition exception
                    TransitionException te = new TransitionException(t, ErrorCodes.FSM_TRANSITION_ERROR, 
                            new Object[]{actionContext.from, actionContext.to, actionContext.event, 
                            actionContext.context, actionContext.action.name(), e.getMessage()});
                    fireEvent(new ExecActionExceptionEventImpl<T, S, E, C>(te, i+1, actionSize, actionContext));
                    throw te;
                } finally {
                    fireEvent(AfterExecActionEventImpl.get(i+1, actionSize, actionContext));
                }
            } else {
                logger.info("Method call action \""+actionContext.action.name()+"\" ("+(i+1)+" of "+actionSize+") was ignored.");
            }
        }
        
        for(Entry<ActionContext<T, S, E, C>, Future<?>> entry : futures.entrySet()) {
            final Future<?> future = entry.getValue();
            final ActionContext<T, S, E, C> actionContext = entry.getKey();
            try {
                logger.debug("Waiting action \'"+actionContext.action.toString()+"\' to finish.");
                if(actionContext.action.timeout()>=0) {
                    future.get(actionContext.action.timeout(), TimeUnit.MILLISECONDS);
                } else {
                    future.get();
                }
                logger.debug("Action \'"+actionContext.action.toString()+"\' finished.");
            } catch (Exception e) {
                future.cancel(true);
                Throwable t = e;
                if(e instanceof ExecutionException) {
                    t = ((ExecutionException)e).getCause();
                }
                TransitionException te = new TransitionException(t, ErrorCodes.FSM_TRANSITION_ERROR, 
                        new Object[]{actionContext.from, actionContext.to, actionContext.event, 
                        actionContext.context, actionContext.action.name(), e.getMessage()});
                fireEvent(new ExecActionExceptionEventImpl<T, S, E, C>(te, 
                        actionContext.position, actionTotalSize, actionContext));
                throw te;
            }
        }
    }
    
    private void executeActions() {
        Pair<String, List<ActionContext<T, S, E, C>>> actionBucket = actionBuckets.poll();
        String bucketName = actionBucket.first();
        List<ActionContext<T, S, E, C>> actionContexts = actionBucket.second();
        doExecute(bucketName, actionContexts);
        logger.debug("Actions within \'"+bucketName+"' invoked.");
    }
    
    @Override
    public void execute() {
        try {
            while(actionBuckets.size()>0) {
                executeActions();
            }
        } finally {
            reset();
        }
    }
    
    @Override
    public void reset() {
        actionBuckets.clear();
        actionTotalSize = 0;
    }
    
    @Override
    public void addExecActionListener(BeforeExecActionListener<T, S, E, C> listener) {
        addListener(BeforeExecActionEvent.class, listener, BeforeExecActionListener.METHOD);
    }
    
    @Override
    public void removeExecActionListener(BeforeExecActionListener<T, S, E, C> listener) {
        removeListener(BeforeExecActionEvent.class, listener);
    }
    
    @Override
    public void addExecActionListener(AfterExecActionListener<T, S, E, C> listener) {
        addListener(AfterExecActionListener.class, listener, AfterExecActionListener.METHOD);
    }
    
    @Override
    public void removeExecActionListener(AfterExecActionListener<T, S, E, C> listener) {
        removeListener(AfterExecActionListener.class, listener);
    }
    
    @Override
    public void addExecActionExceptionListener(ExecActionExceptionListener<T, S, E, C> listener) {
        addListener(ExecActionExceptionEvent.class, listener, ExecActionExceptionListener.METHOD);
    }
    
    @Override
    public void removeExecActionExceptionListener(ExecActionExceptionListener<T, S, E, C> listener) {
        removeListener(ExecActionExceptionEvent.class, listener);
    }
    
    @Override
    public void setDummyExecution(boolean dummyExecution) {
        this.dummyExecution = dummyExecution;
    }
    
    static class ExecActionExceptionEventImpl<T extends StateMachine<T, S, E, C>, S, E, C> 
        extends AbstractExecActionEvent<T, S, E, C> implements ExecActionExceptionEvent<T, S, E, C> {
        
        private final TransitionException e;

        ExecActionExceptionEventImpl(TransitionException e, int pos, int size, ActionContext<T, S, E, C> actionContext) {
            super(pos, size, actionContext);
            this.e = e;
        }

        @Override
        public TransitionException getException() {
            return e;
        }
        
    }
    
    static class BeforeExecActionEventImpl<T extends StateMachine<T, S, E, C>, S, E, C> 
            extends AbstractExecActionEvent<T, S, E, C> implements BeforeExecActionEvent<T, S, E, C> {
        
        BeforeExecActionEventImpl(int pos, int size, ActionContext<T, S, E, C> actionContext) {
            super(pos, size, actionContext);
        }

        static <T extends StateMachine<T, S, E, C>, S, E, C> BeforeExecActionEvent<T, S, E, C> get(
                int pos, int size, ActionContext<T, S, E, C> actionContext) {
            return new BeforeExecActionEventImpl<T, S, E, C>(pos, size, actionContext);
        }
    }
    
    static class AfterExecActionEventImpl<T extends StateMachine<T, S, E, C>, S, E, C>
            extends AbstractExecActionEvent<T, S, E, C> implements AfterExecActionEvent<T, S, E, C> {

        AfterExecActionEventImpl(int pos, int size, ActionContext<T, S, E, C> actionContext) {
            super(pos, size, actionContext);
        }

        static <T extends StateMachine<T, S, E, C>, S, E, C> AfterExecActionEvent<T, S, E, C> get(
                int pos, int size, ActionContext<T, S, E, C> actionContext) {
            return new AfterExecActionEventImpl<T, S, E, C>(pos, size, actionContext);
        }
    }
    
    static abstract class AbstractExecActionEvent<T extends StateMachine<T, S, E, C>, S, E, C> 
            implements ActionEvent<T, S, E, C> {
        private ActionContext<T, S, E, C> executionContext;
        private int pos;
        private int size;
        
        AbstractExecActionEvent(int pos, int size, ActionContext<T, S, E, C> actionContext) {
            this.pos = pos;
            this.size = size;
            this.executionContext = actionContext;
        }
        
        @Override
        public Action<T, S, E, C> getExecutionTarget() {
            // user can only read action info but cannot invoke action in the listener method
            return new UncallableActionImpl<T, S, E, C>(executionContext.action);
        }

        @Override
        public S getFrom() {
            return executionContext.from;
        }

        @Override
        public S getTo() {
            return executionContext.to;
        }

        @Override
        public E getEvent() {
            return executionContext.event;
        }

        @Override
        public C getContext() {
            return executionContext.context;
        }

        @Override
        public T getStateMachine() {
            return executionContext.fsm;
        }

        @Override
        public int[] getMOfN() {
            return new int[]{pos, size};
        }
    }
    
    static class ActionContext<T extends StateMachine<T, S, E, C>, S, E, C> {
        final Action<T, S, E, C> action;
        final S from;
        final S to;
        final E event;
        final C context;
        final T fsm;
        final int position;
        
        private ActionContext(Action<T, S, E, C> action, S from, S to, E event, C context, T stateMachine, int position) {
            this.action = action;
            this.from = from;
            this.to = to;
            this.event = event;
            this.context = context;
            this.fsm = stateMachine;
            this.position = position;
        }
        
        static <T extends StateMachine<T, S, E, C>, S, E, C> ActionContext<T, S, E, C> get(
                Action<T, S, E, C> action, S from, S to, E event, C context, T stateMachine, int position) {
            return new ActionContext<T, S, E, C>(action, from, to, event, context, stateMachine, position);
        }

        void run() {
            AbstractStateMachine<T, S, E, C> fsmImpl = (AbstractStateMachine<T, S, E, C>)fsm;
            fsmImpl.beforeActionInvoked(from, to, event, context);
            action.execute(from, to, event, context, fsm);
            fsmImpl.afterActionInvoked(from, to, event, context);
        }
    }
}
