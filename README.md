squirrel-foundation
========

### What is it?  
**squirrel-foundation** provided an easy use, type safe and highly extensible state machine implementation for Java.  

### Maven  
```maven
<dependency>
  <groupId>org.squirrelframework</groupId>
  <artifactId>squirrel-foundation</artifactId>
  <version>0.1.5</version>
</dependency>
``` 

### Getting Started 

**squirrel-foundation** supports both fluent API and declarative manner to define a state machine, and also enable user to declare the action methods in a straightforward manner. 

* **StateMahcine** takes four type parameters.  
	* **T** The type of implemented state machine.
	* **S** The type of implemented state.
	* **E** The type of implemented event.
	* **C** The type of implemented context.

* **StateMachineBuilder**  
	* The StateMachineBuilder is composed of TransitionBuilder which is used to build transition between states and EntryExitActionBuilder which is used to build the actions during entry or exit state. 
	* The state of StateMachine is implicitly built during transition build and state action build.
	e.g. new state machine builder
	
		```java
		StateMachineBuilder<ConventionalStateMachine, MyState, MyEvent, MyContext> builder =
			StateMachineBuilderImpl.newStateMachineBuilder(ConventionalStateMachine.class, 
			MyState.class, MyEvent.class, MyContext.class);
		```

* **Fluent API**  
```java
builder.transition().from(MyState.A).to(MyState.B).on(MyEvent.GoToB);
```
An **external transition** is built from state 'A' to state 'B' on event 'GoToB'.
```java
builder.transition().within(MyState.A).on(MyEvent.WithinA).perform(myAction);
```
An **internal transition** is build inside state 'A' on event 'WithinA' perform 'myAction'. The internal transition means after transition complete, no state is exited or entered.
```java
		builder.transition().from(MyState.C).to(MyState.D).on(MyEvent.GoToD).when(
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
	User can define actions during define transitions or state entry actions. However, the actions will be scattered over the definitions method blocks and other classes. Moreover, other user cannot override the actions. Thus, we also support define method call actions within state machine implementation in a **Convention Over Configuration** manner.  
	Basically, this means that if the method declared in state machine satisfied naming and parameters convention, it will be added into the transition action list and also be invoked at certain transition status. e.g.  
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
	
	***Other supported naming pattern***
	```
	transitFrom[fromStateName]To[toStateName]On[eventName]When[conditionName]  
    transitFrom[fromStateName]To[toStateName]On[eventName]  
    transitFromAnyTo[toStateName]On[eventName]  
    transitFrom[fromStateName]ToAnyOn[eventName]  
    transitFrom[fromStateName]To[toStateName]          
    on[eventName] 
    ```
* **Declarative Annotation**  
Use conventional way to define action method call is convenient, but sometimes user may want to give method a more meaningful name, and moreover the java compiler cannot help user to detect the error when misspelling the method name. For this case, a declarative way is also provided to define or extend the state machine. Here is an example.  
	```java
	@States({
        @State(name="A", entryCallMethod="entryStateA", exitCallMethod="exitStateA"), 
        @State(name="B", entryCallMethod="entryStateB", exitCallMethod="exitStateB")
    })
	@Transitions({
        @Transit(from="A", to="B", on="GoToB", callMethod="stateAToStateBOnGotoB"),
        @Transit(from="A", to="A", on="WithinA", callMethod="stateAToStateAOnWithinA", type=TransitionType.INTERNAL)
	})
	interface DeclarativeStateMachine extends StateMachine<DeclarativeStateMachine, TestState, TestEvent, Integer> { ... }
	```
	The annotation can be defined in both implementation class of state machine or any interface that state machine will be implemented. Moreover, this declarative annotations can also be used together with fluent API which means the state machine defined in fluent API can also be extended by these annotations. (One thing you may need to be noticed, the method defined within interface must be public, which means also the method call action implementation will be public to caller.)  
	
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
Then register to *ConverterProvider*. e.g.
```java
ConverterProvider.INSTANCE.register(MyEvent.class, new MyEventConverter());
ConverterProvider.INSTANCE.register(MyState.class, new MyStateConverter());
```  
	Note: If you only use fluent API to define state machine, there is no need to implement corresponding converters.
	
* **New State Machine Instance**  
```java
T newStateMachine(S initialStateId, T parent, Class<?> type, boolean isLeaf, Object... extraParams);
```
To create a new state machine instance from state machine builder, you need to pass several parameters.
	1. *initialStateId*: When started, the initial state of the state machine.
	2. *parent*: parent state machine of current. Set null for no parent state machine.
	3. *type*: the type of state machine value. Set to *Object.class* for no need to be specified.
	4. *isLeaf*: whether current state machine has child state machines. Set true for no child.
	5. *extraParams*: Extra parameters that needed for create new state machine instance. Set to "*new Object[0]*" for no extra parameters needed.  
	
	New state machine from state machine builder.
	```java
	MyStateMachine stateMachine = builder.newStateMachine(MyState.Initial, null, Object.class, true, new Object[0]);
	```

* **Fire events**  
	After state machine was created, user can fire events along with context to trigger state transition inside state machine. e.g.
	```java
	stateMachine.fire(MyEvent.Prepare, new MyContext("Testing"));
	```

### Advanced Feature
* **Hierarchical State Machine** 

* **State Machine Lifecycle Events**  
During the lifecycle of the state machine, various events will be fired. TBD.

* **State Machine PostProcessor**  
TBD
* **State Machine Intercepter**  
TBD
* **State Machine Diagnose**  
TBD 

### Future Plan  
* Support state persistence
* Support sendEvent(sync) and postEvent(async)
