squirrel-foundation
========

### What is it?  
**squirrel-foundation** provided an easy use, type safe and highly extensible **state machine** ([Wikipedia] [1]) implementation for Java.  

### Maven  
```maven
<dependency>
  <groupId>org.squirrelframework</groupId>
  <artifactId>squirrel-foundation</artifactId>
  <version>0.1.6</version>
</dependency>
``` 

### Getting Started 

**squirrel-foundation** supports both fluent API and declarative manner to declare a state machine, and also enable user to define the action methods in a straightforward manner. 

* **StateMahcine** interface takes four generic type parameters.  
	* **T** stands for the type of implemented state machine.
	* **S** stands for the type of implemented state.
	* **E** stands for the type of implemented event.
	* **C** stands for the type of implemented context.

* **StateMachineBuilder**  
	* The StateMachineBuilder is composed of *TransitionBuilder which is used to build transition between states and EntryExitActionBuilder which is used to build the actions during entry or exit state. 
	* The internal state is implicitly built during transition creation or state action creation. In order to create a state machine, user need to create state machine builder first. For example: 
	
		```java
		StateMachineBuilder<ConventionalStateMachine, MyState, MyEvent, MyContext> builder =
			StateMachineBuilderImpl.newStateMachineBuilder(ConventionalStateMachine.class, 
			MyState.class, MyEvent.class, MyContext.class);
		```

* **Fluent API**  
After state machine builder was created, we can use fluent API to define state/transition/action of the state machine.
```java
builder.externalTransition().from(MyState.A).to(MyState.B).on(MyEvent.GoToB);
```
An **external transition** is built from state 'A' to state 'B' on event 'GoToB'.
```java
builder.internalTransition().within(MyState.A).on(MyEvent.WithinA).perform(myAction);
```
An **internal transition** is build inside state 'A' on event 'WithinA' perform 'myAction'. The internal transition means after transition complete, no state is exited or entered.
```java
		builder.externalTransition().from(MyState.C).to(MyState.D).on(MyEvent.GoToD).when(
		new Condition<MyContext>() {
            @Override
            public boolean isSatisfied(MyContext context) {
                return context!=null && context.getValue()>80;
            }
        });
```
An **conditional transition** is built from state 'C' to state 'D' on event 'GoToD' when external context satisfied the condition restriction.
```java
builder.onEntry(MyState.A).perform(Lists.newArrayList(action1, action2))
```
A list of state entry actions is defined.

* **Method Call Action**  
	User can define actions during define transitions or state entry actions. However, the actions will be scattered over the definitions method blocks and other classes. Moreover, other user cannot override the actions. Thus, squirrel-foundation also support define method call actions within state machine implementation in a **Convention Over Configuration** manner.  
	Basically, this means that if the method declared in state machine satisfied naming and parameters convention, it will be added into the transition action list and also be invoked at certain phase. e.g.  
	```java
	protected void transitFromAToBOnGoToB(MyState from, MyState to, MyEvent event, MyContext context)
	```
	The method named as **transitFrom\[SourceStateName\]To\[TargetStateName\]On\[EventName\]**, and parameterized as \[MyState, MyState, MyEvent, MyContext\] will be added into transition "A-(GoToB)->B" action list. When transiting from state 'A' to state 'B' on event 'GoToB', this method will be invoked.
	
	```java
	protected void transitFromAnyToBOnGoToB(MyState from, MyState to, MyEvent event, MyContext context)
	```
	**transitFromAnyTo[TargetStateName]On[EventName]** The method will be invoked when transit from any state to state 'B' on event 'GoToB'.
	```java
	protected void exitA(MyState from, MyState to, MyEvent event, MyContext context)
	```
	**exit[StateName]** The method will be invoked when exit state 'A'. So as the **entry[StateName]** , **exitAny** and **entryAny**.  
	
	***Other Supported Naming Patterns:***
	```
	transitFrom[fromStateName]To[toStateName]On[eventName]When[conditionName]  
    transitFrom[fromStateName]To[toStateName]On[eventName]  
    transitFromAnyTo[toStateName]On[eventName]  
    transitFrom[fromStateName]ToAnyOn[eventName]  
    transitFrom[fromStateName]To[toStateName]          
    on[eventName] 
    ```
* **Declarative Annotation**  
Use conventional way to define action method call is convenient, but sometimes user may want to give method a more meaningful name. Moreover, the java compiler cannot help user to detect the error when misspelling the method name. For this case, a declarative way is also provided to define and also to extend the state machine. Here is an example.  
	```java
	@States({
        @State(name="A", entryCallMethod="entryStateA", exitCallMethod="exitStateA"), 
        @State(name="B", entryCallMethod="entryStateB", exitCallMethod="exitStateB")
    })
	@Transitions({
        @Transit(from="A", to="B", on="GoToB", callMethod="stateAToStateBOnGotoB"),
        @Transit(from="A", to="A", on="WithinA", callMethod="stateAToStateAOnWithinA", type=TransitionType.INTERNAL)
	})
	interface MyStateMachine extends StateMachine<MyStateMachine, MyState, MyEvent, MyContext> {
		void entryStateA(MyState from, MyState to, MyEvent event, MyContext context);
		void stateAToStateBOnGotoB(MyState from, MyState to, MyEvent event, MyContext context)
		void stateAToStateAOnWithinA(MyState from, MyState to, MyEvent event, MyContext context)
		void exitStateA(MyState from, MyState to, MyEvent event, MyContext context);
		...
	}
	```
	The annotation can be defined in both implementation class of state machine or any interface that state machine will be implemented. It also can be used mixed with fluent API, which means the state machine defined in fluent API can also be extended by these annotations. (One thing you may need to be noticed, the method defined within interface must be public, which means also the method call action implementation will be public to caller.)  
	
* **Converters**  
In order to declare state and event within *@State* and *@Transit*, user need to implement corresponding converters for their state(S) and event(E) type. The convert must implement Converter\<T\> interface, which convert the state/event to/from String.
```java
public interface Converter<T> extends SquirrelComponent {
    /**
     * Convert object to string.
     * @param obj converted object
     * @return string description of object
     */
    String convertToString(T obj);
    
    /**
     * Convert string to object.
     * @param name name of the object
     * @return converted object
     */
    T convertFromString(String name);
}
```  
Then register these converters to *ConverterProvider*. e.g.
```java
ConverterProvider.INSTANCE.register(MyEvent.class, new MyEventConverter());
ConverterProvider.INSTANCE.register(MyState.class, new MyStateConverter());
```  
	Note: If you only use fluent API to define state machine, there is no need to implement corresponding converters.
	
* **New State Machine Instance**  
After user defined state machine behavior, user could create a new state machine instance through builder. Note, once the state machine instance is created from the builder, the builder cannot used to define any new element of state machine anymore.
```java
T newStateMachine(S initialStateId, Object... extraParams);
```
To create a new state machine instance from state machine builder, you need to pass following parameters.
	1. *initialStateId*: When started, the initial state of the state machine.
	2. *extraParams*: Extra parameters that needed for create new state machine instance. Set to *"new Object[0]"* for no extra parameters needed.  
	
	New state machine from state machine builder.
	```java
	MyStateMachine stateMachine = builder.newStateMachine(MyState.Initial, new Object[0]);
	```

* **Fire Events**  
	After state machine was created, user can fire events along with context to trigger transition inside state machine. e.g.
	```java
	stateMachine.fire(MyEvent.Prepare, new MyContext("Testing"));
	```

### Advanced Feature
* **Define Hierarchical State**  
The hierarchical state can be defined through API or annotation.
```java
void defineHierachyOn(S parentStateId, S... childStateIds);
```
*builder.defineHierarchyOn(State.A, State.BinA, StateCinA)* defines two child states "BinA" and "CinA" under parent state "A", the first defined child state will also be the initial state of the hierarchical state "A". The same hierarchical state can also be defined through annotation, e.g.
```java
@States({
		@State(name="A", entryMethodCall="entryA", exitMethodCall="exitA"),
		@State(parent="A", name="BinA", entryMethodCall="entryBinA", exitMethodCall="exitBinA", initialState=true),
		@State(parent="A", name="CinA", entryMethodCall="entryCinA", exitMethodCall="exitCinA")
})
```  

* **Define Parallel State**  
TBD

* **Using History States to Save and Restore the Current State**  
TBD

* **Transition Types**  
According to the UML specification, a transition may be one of these three kinds:    

	> * *Internal Transition*  	
Implies that the Transition, if triggered, occurs without exiting or entering the source State (i.e., it does not cause a state change). This means that the entry or exit condition of the source State will not be invoked. An internal Transition can be taken even if the StateMachine is in one or more Regions nested within the associated State.  
	> * *Local Transition*  
	Implies that the Transition, if triggered, will not exit the composite (source) State, but it will exit and re-enter any state within the composite State that is in the current state configuration.
	> * *External Transition*   
	Implies that the Transition, if triggered, will exit the composite (source) State
	  

	squirrel-foundation supports both API and annotation to declare all kinds of transitions, e.g.  
	```java
	builder.externalTransition().from(MyState.A).to(MyState.B).on(MyEvent.A2B);
	builder.internalTransition().within(MyState.A).on(MyEvent.innerA);
	builder.localTransition().from(MyState.A).to(MyState.CinA).on(MyEvent.intoC)
	```
	or  
	```java
	@Transitions({
		@Transition(from="A", to="B", on="A2B"), //default value of transition type is EXTERNAL
		@Transition(from="A", on="innerA", type=TransitionType.INTERNAL),
		@Transition(from="A", to="CinA", on="intoC", type=TransitionType.LOCAL),
	})
	```

* **State Machine Lifecycle Events**  
During the lifecycle of the state machine, various events will be fired, e.g. 
```  
|--StateMachineEvent 						/* Base event of all state machine event */   
       |--StartEvent							/* Fired when state machine started      */ 
       |--TerminateEvent						/* Fired when state machine terminated   */ 
          |--TransitionEvent					/* Base event of all transition event    */ 
             |--TransitionBeginEvent			/* Fired when transition began           */ 
             |--TransitionCompleteEvent			/* Fired when transition completed       */ 
             |--TransitionExceptionEvent		/* Fired when transition threw exception */ 
             |--TransitionDeclinedListener		/* Fired when transition declined        */ 
```
User can add a listener to listen StateMachineEvent, which means all events fired during state machine lifecycle will be caught by this listener, e.g.,
```java
stateMachine.addListener(new StateMachineListener<MyStateMachine, MyState, MyEvent, MyContext>() {
			@Override
			public void stateMachineEvent(StateMachineEvent<MyStateMachine, MyState, MyEvent, MyContext> event) {
				// ...
			}
	});
```

* **State Machine PostProcessor**  
	User can register post processor for specific type of state machine in order to adding post process logic after state machine instantiated, e.g.  
	```java
	// 1 User defined a state machine interface
	interface MyStateMachine extends StateMachine<MyStateMachine, MyState, MyEvent, MyContext> {
	. . .
	}
	
	// 2 Both MyStateMachineImpl and MyStateMachineImplEx are implemented MyStateMachine
	class MyStateMachineImpl implements MyStateMachine {
		. . . 
	}
	class MyStateMachineImplEx implements MyStateMachine {
		. . .
	}
	
	// 3 User define a state machine post processor
	MyStateMachinePostProcessor implements SquirrelPostProcessor<MyStateMachine> {
		void postProcess(MyStateMachine component) {
			. . . 
		}
	}  
	
	// 4 User register state machine post process
	SquirrelPostProcessorProvider.getInstance().register(MyStateMachine.class, MyStateMachinePostProcessor.class);
	```
	For this case, when user created both MyStateMachineImpl and MyStateMachineImplEx instance, the registered post processor MyStateMachinePostProcessor will be called to do some work.
* **State Machine Intercepter**  
	User can register intercepter for specific type of state machine to insert custom logic during state machine lifecycle. 
	```java
	public abstract class AbstractStateMachineIntercepter<T extends StateMachine<T, S, E, C>, S, E, C> 
    	implements StateMachineIntercepter<T, S, E, C>, SquirrelPostProcessor<T> {
    	. . .
    }
	```
	User can insert custom logic at different state machine process phases by creating a state machine intercepter which is extended from *AbstractStateMachineIntercepter*. Actually, the *AbstractStateMachineIntercepter* also implemented *SquirrelPostProcessor* interface. It will add a StateMachineEvent listener into the state machine, and dispatch the method call according to the event type. Thus, the StateMachineIntercepter registration should be the same as state machine post processor.   
	By leveraging state machine intercepter, user can implement various monitors for performance analysis, exception diagnose and so on.
	
* **State Machine Diagnose**  
	User can register various monitors as state machine intercepter to observe internal status of the state machine, like the execution performance, action calling sequence, transition progress and so on.   
	For example, the following code is used to register an execution time monitor for state machine of *MyStateMachine* type.
	```java
	SquirrelPostProcessorProvider.getInstance().register(MyStateMachine.class, 
        		new TypeReference<TransitionExecTimeMonitor<MyStateMachine, MyState, MyEvent, MyContext>>() {});
	```  
	The following code is used to monitor transition progress by adding a *TransitionProgressMonitor* to *ActionExecutor* to monitor transition action execution.
	```java
	SquirrelPostProcessorProvider.getInstance().register(
        new TypeReference<ActionExecutor<MyStateMachine, MyState, MyEvent, MyContext>>(){}, 
        new SquirrelPostProcessor<ActionExecutor<MyStateMachine, MyState, MyEvent, MyContext>>() {
			@Override
            public void postProcess(ActionExecutor<MyStateMachine, MyState, MyEvent, MyContext> component) {
				component.addListener(new TransitionProgressMonitor<MyStateMachine, MyState, MyEvent, MyContext>());
            }
        }
	);
	```   
	Add **@LogExecTime** on action method will log out the execution time of the method. And also add the @LogExecTime on state machine class will log out all the action method execution time. For example, the execution time of method *transitFromAToBOnGoToB* will be logged out.
	```java
	@LogExecTime
	protected void transitFromAToBOnGoToB(MyState from, MyState to, MyEvent event, MyContext context)
	```

### Future Plan  
* Support state persistence
* Support sendEvent(sync) and postEvent(async)

[1]: http://en.wikipedia.org/wiki/UML_state_machine
