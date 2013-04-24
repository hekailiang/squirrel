package org.squirrelframework.foundation.fsm.impl;

import java.util.LinkedList;
import java.util.Queue;

import org.squirrelframework.foundation.fsm.DotVisitor;
import org.squirrelframework.foundation.fsm.HistoryType;
import org.squirrelframework.foundation.fsm.ImmutableState;
import org.squirrelframework.foundation.fsm.ImmutableTransition;
import org.squirrelframework.foundation.fsm.StateMachine;

class DotVisitorImpl<T extends StateMachine<T, S, E, C>, S, E, C> extends AbstractVisitor implements DotVisitor<T, S, E, C> {

    protected final StringBuilder transBuf = new StringBuilder();
    
    @Override
    public void visitOnEntry(StateMachine<T, S, E, C> visitable) {
        writeLine("digraph {\ncompound=true;");
        writeLine("subgraph cluster_StateMachine {\nlabel=\""+visitable.getClass().getName()+"\";");
    }

    @Override
    public void visitOnExit(StateMachine<T, S, E, C> visitable) {
        buffer.append(transBuf);
        writeLine("}}");
    }

    @Override
    public void visitOnEntry(ImmutableState<T, S, E, C> visitable) {
        String stateId = visitable.getStateId().toString();
        if(visitable.hasChildStates()) {
            writeLine("subgraph cluster_"+stateId+" {\nlabel=\""+stateId+"\";");
            if(visitable.getHistoryType()==HistoryType.DEEP) {
                writeLine(stateId+"History"+" [label=\"\"];");
            } else if (visitable.getHistoryType()==HistoryType.SHALLOW) {
                writeLine(stateId+"History"+" [label=\"\"];");
            }
        } else {
            writeLine(stateId+" [label=\""+stateId+"\"];");
        }
    }

    @Override
    public void visitOnExit(ImmutableState<T, S, E, C> visitable) {
        if(visitable.hasChildStates()) {
            writeLine("}");
        }
    }

    @Override
    public void visitOnEntry(ImmutableTransition<T, S, E, C> visitable) {
        ImmutableState<T, S, E, C> sourceState = visitable.getSourceState();
        ImmutableState<T, S, E, C> targetState = visitable.getTargetState();
        String sourceStateId = sourceState.getStateId().toString();
        String targetStateId = targetState.getStateId().toString();
        boolean sourceIsCluster=sourceState.hasChildStates();
        boolean targetIsCluster=targetState.hasChildStates();
        String source=(sourceIsCluster)?"cluster_"+sourceStateId:null;
        String target=(targetIsCluster)?"cluster_"+targetStateId:null;
        String realStart=(sourceIsCluster)? getSimpleChildOf(sourceState).getStateId().toString():sourceStateId;
        String realEnd=(targetIsCluster)? getSimpleChildOf(targetState).getStateId().toString():targetStateId;
        String edgeLabel = visitable.getEvent().toString();
        String ltail=(source!=null)?"ltail=\""+source+"\"":null;
        String lhead=(target!=null)?"lhead=\""+target+"\"":null;
        transBuf.append("\n"+realStart+" -> "+realEnd+" ["+((ltail!=null)?ltail+",":"")+((lhead!=null)?lhead+",":"")+" label=\""+edgeLabel+"\"];");
    }
    
    public ImmutableState<T, S, E, C> getSimpleChildOf(ImmutableState<T, S, E, C> sourceState) {
        Queue<ImmutableState<T, S, E, C>> list=new LinkedList<ImmutableState<T, S, E, C>>();
        list.add(sourceState);
        while(!list.isEmpty()) {
            ImmutableState<T, S, E, C> x=list.poll();
            int l=x.getChildStates().size();
            for (int i=0; i<l; i++) {
                ImmutableState<T, S, E, C> c = x.getChildStates().get(i);
                if (c.hasChildStates()) list.add(c);
                else return c;
            }
        }
        return sourceState;
    }

    @Override
    public void visitOnExit(ImmutableTransition<T, S, E, C> visitable) {
        
    }

    @Override
    public void convertDotFile(String filename) {
        saveFile(filename+".dot", buffer.toString());
    }

}
