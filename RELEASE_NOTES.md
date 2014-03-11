*Version 0.3.1 - 2014-3-12* 
---
1. Fixed [Issue 14](https://github.com/hekailiang/squirrel/issues/14) - Event fired against wrong state machine when FSMs collaborate   

*Version 0.3.1 - 2014-3-4*  
---
1. Fix action execution position bug  
2. Support custom listener order and fix listener order caused issue  
3. Support fire event immediately, StateMachine.fireImmediate  
4. Fix state machine initial get current state issue  
5. Support method extension from fluent API and annotation  

*Version 0.3.0 - 2014-2-21*
---
1. **Remove** deprecated state machine intercepter classes  
2. **Deprecate** StateMachineProvider  
3. Support **postConstruct** after state machine initialized  
4. **Remove** deprecated exception handling method in AbstractStateMachine  
5. Add API StateMachine.canAccept(E event)  
6. Improve state machine create exception handling  
7. **Breaking Change:** Reimplement StateMachinConfiguration to simplify its usage    
8. More easy way to enable state machine logger by set state machine debug configure to true  
9. Allow configuration to be set on state machine builder  
10. Make both StateMachineData.Reader and StateMachineData.Writer serializable  

*Version 0.2.9 - 2014-2-19*
---
1. Support state machine behavior configuration  
2. **Breaking Change:** Removed tedious state machine constructor from *AbstractStateMachine*. Please remove extended constructor in your state machine implementation class.  

*Version 0.2.8 - 2014-2-17*
--- 
1. Action execution bug fixed. State exit actions(both sync/async) should be all executed successfully then begin transition actions, so as the state entry actions.   
2. Add debug loggings while action execution.

*Version 0.2.7 - 2014-2-13*  
---
1. **Remove** deprecated StateMachineWithoutContext Class and related artifacts  
2. **Remove** deprecated OnActionExecute annotation  
3. Support state machine data serialize/deserialize by ObjectSerializableSupport  
4. Add API StateMachine.getLastException  
5. **Deprecate** AbstractStateMachine.afterTransitionCausedException(TransitionException e, ...)  

*Version 0.2.6 - 2014-2-9*  
---
1. Fix bugs around linked state feature  
2. Support timeout for asynchronized action  
3. Improve state transition performance [Issue 11][11] (Thanks to Jeremy)  
4. Add **StateMachinePerformanceMonitor** to help analysing performance issue  
5. Fix data correctness issues including test event, data isolation during transition and so on  
6. Implement **State Machine Importer** feature  
7. Add API ImmutableState.getAcceptableEvents()  
8. Add API StateMachine.exportXMLDefinition(boolean) to simplify state machine export usage   
9. **Remove** UntypedStateMachineBuilder.newUntypedStateMachine(Object, Class) method  
10. **Deprecate** AbstractCondition, replaced with **AnonymousCondition**  
11. **Deprecate** AnonymousUntypedAction, replaced with **UntypedAnonymousAction**  
  
*Version 0.2.5 - 2014-1-19*  
---
1. Support asynchronized method call execution  
2. Support asynchronized declarative event dispatch  
3. Support timed state  
4. Allow to log per action method call execution time  
5. Add Before/After action executed event listener(@OnBeforeActionExecuted/@OnAfterActionExecuted)  
6. **Deprecate** OnActionExecute annotation  
7. **Deprecate** UntypedStateMachineBuilder.newUntypedStateMachine(Object, Class<T>) method  

*Version 0.2.4 - 2014-1-16*  
---
1. **Breaking Change**: Fix a typo StateMachineParamters -> StateMachineParameters  

*Version 0.2.3 - 2014-1-15*  
---
1. Support weighted action  
2. **Breaking Change**: Add name() and weight() to Action interface(use AnonymousAction instead)  
3. Update SCXML export content  
4. Improve exception handling logic including error message  
5. Deprecate StateMachineIntercepter and StateMachineWithoutContext  

*Version 0.2.2.5 - 2013-12-26*  
---
1. Support prioritized transition  
2. Provide thread-safe implementation of StateMachine  
3. Update current states of state machine after actions all executed successfully  
4. **Support transition condition and action defined in MVEL script**  
5. **Breaking Change**: Add name() to Condition interface  
6. **Add UntypedStateMachine to simplify usage**  
7. Support build method call action through transition builder API  
8. Add debug log when method call cannot find  
9. **Support add declarative event listener**  
10. Add **StateMachineLogger** to easy monitor state machine execution status  
11. Update documentation

*Version 0.2.1 - 2013-08-10*  
---
1. Support test State machine transition result  
2. Add *StateMachineWithoutContext* class to simplify state machine usage without need of context  
3. Bug fixed for state machine data dump

*Version 0.1.10 - 2013-07-13*  
---
1. Support save/load state machine data
  
*Version 0.1.9  - 2013-07-01*  
---
1. Add to *StateMachineBuilderFactory* simplify StateMachineBuilder creation  
2. Deprecate StateMachineBuilderImpl.newStateMachineBuilder(...) methods
 
*Version 0.1.8  - 2013-06-08*  
---
1. Support **linked state** which also called substatemachine state  
2. Rename addListener/removeListener of StateMachine to more specific name add\*Listener/remove\*Listener  
3. Simplify converter registration for String and Enumeration type  
 