package org.squirrelframework.foundation.fsm;

import org.squirrelframework.foundation.fsm.annotation.EventType;

public enum TestEvent {
	@EventType(EventKind.START)
	Started,
	
    ToEnd, ToA, InternalA, ToB, ToC, ToD, 
    
    @EventType(EventKind.FINISH) 
    Finished,
    
    @EventType(EventKind.TERMINATE)
    Terminated
}
