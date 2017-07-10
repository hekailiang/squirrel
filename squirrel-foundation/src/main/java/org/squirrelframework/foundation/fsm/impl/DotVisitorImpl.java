package org.squirrelframework.foundation.fsm.impl;

import java.util.LinkedList;
import java.util.Queue;

import org.squirrelframework.foundation.fsm.DotVisitor;
import org.squirrelframework.foundation.fsm.HistoryType;
import org.squirrelframework.foundation.fsm.ImmutableState;
import org.squirrelframework.foundation.fsm.ImmutableTransition;
import org.squirrelframework.foundation.fsm.StateMachine;

class DotVisitorImpl extends AbstractVisitor implements DotVisitor {

    protected final StringBuilder transBuf = new StringBuilder();

    @Override
    public void visitOnEntry(StateMachine<?, ?, ?, ?> visitable) {
        writeLine("digraph {\ncompound=true;");
        writeLine("subgraph cluster_StateMachine {\nlabel=\"" + visitable.getClass().getName() + "\";");
    }

    @Override
    public void visitOnExit(StateMachine<?, ?, ?, ?> visitable) {
        buffer.append(transBuf);
        writeLine("}}");
    }

    @Override
    public void visitOnEntry(ImmutableState<?, ?, ?, ?> visitable) {
        String stateId = getName(visitable.getStateId());
        String stateLabel = visitable.getStateId().toString();
        if (visitable.hasChildStates()) {
            writeLine("subgraph cluster_" + stateId + " {\nlabel=\"" + stateLabel + "\";");
            if (visitable.getHistoryType() == HistoryType.DEEP) {
                writeLine(stateId + "History" + " [label=\"\"];");
            } else if (visitable.getHistoryType() == HistoryType.SHALLOW) {
                writeLine(stateId + "History" + " [label=\"\"];");
            }
        } else {
            writeLine(stateId + " [label=\"" + stateLabel + "\"];");
        }
    }

    @Override
    public void visitOnExit(ImmutableState<?, ?, ?, ?> visitable) {
        if (visitable.hasChildStates()) {
            writeLine("}");
        }
    }

    @Override
    public void visitOnEntry(ImmutableTransition<?, ?, ?, ?> visitable) {
        ImmutableState<?, ?, ?, ?> sourceState = visitable.getSourceState();
        ImmutableState<?, ?, ?, ?> targetState = visitable.getTargetState();
        String sourceStateId = getName(sourceState.getStateId());
        String targetStateId = getName(targetState.getStateId());
        boolean sourceIsCluster = sourceState.hasChildStates();
        boolean targetIsCluster = targetState.hasChildStates();
        String source = (sourceIsCluster) ? "cluster_" + sourceStateId : null;
        String target = (targetIsCluster) ? "cluster_" + targetStateId : null;
        String realStart = (sourceIsCluster) ? getName(getSimpleChildOf(sourceState).getStateId()) : sourceStateId;
        String realEnd = (targetIsCluster) ? getName(getSimpleChildOf(targetState).getStateId()) : targetStateId;
        String edgeLabel = visitable.getEvent().toString();
        String ltail = (source != null) ? "ltail=\"" + source + "\"" : null;
        String lhead = (target != null) ? "lhead=\"" + target + "\"" : null;
        transBuf.append(
            "\n" + realStart + " -> " + realEnd + " [" + ((ltail != null) ? ltail + "," : "") + ((lhead != null) ? lhead
                + "," : "") + " label=\"" + edgeLabel + "\"];");
    }

    public ImmutableState<?, ?, ?, ?> getSimpleChildOf(ImmutableState<?, ?, ?, ?> sourceState) {
        Queue<ImmutableState<?, ?, ?, ?>> list = new LinkedList<ImmutableState<?, ?, ?, ?>>();
        list.add(sourceState);
        while (!list.isEmpty()) {
            ImmutableState<?, ?, ?, ?> x = list.poll();
            int l = x.getChildStates().size();
            for (int i = 0; i < l; i++) {
                ImmutableState<?, ?, ?, ?> c = x.getChildStates().get(i);
                if (c.hasChildStates()) { list.add(c); } else { return c; }
            }
        }
        return sourceState;
    }

    @Override
    public void visitOnExit(ImmutableTransition<?, ?, ?, ?> visitable) {
    }

    @Override
    public void convertDotFile(String filename) {
        saveFile(filename + ".dot", buffer.toString());
    }

}
