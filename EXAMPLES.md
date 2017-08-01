Examples
---
* **ATM State Machine - Example on context insensitive typed state machine**  
The sample code could be found in package *"org.squirrelframework.foundation.fsm.atm"*.  

* **Decision State Machine - Example on local transition**  
	This example demonstrated how to leverage local transition and nested state to create a decision node.
	![DecisionStateMachine](http://hekailiang.github.io/squirrel/images/decisionfsm.png)  
	This example can be found in package *"org.squirrelframework.foundation.fsm.samples.DecisionStateSampleTest"*. 

* **Simple CSS Parser - Example usage of fluent API**  
	This example illustrates how to parse incoming characters by define parser grammar in state machine.  
	![SimpleCssParser](http://hekailiang.github.io/squirrel/images/SimpleCssParser.png)  
	Parse CSS scripts with *SimpleCssParser* which is defined as State Machine.
	```java
	SimpleCssParser parser = SimpleCssParser.newParser();
    List<CssRule> rules = parser.parse("alpha { width: 100px/*comment1*/; /*comment2*/text-decoration: " + 
    	"/*comment3*/ underlined; } epsilon/*comment4*/, zeta{ height: 34px; } ");
	```
	Sample code to define CssParser could be found in package *"org.squirrelframework.foundation.fsm.cssparser"*.

* **Greedy Snake Game Sample - Sample usage of declarative untyped state machine and timed state**  
	Here is an interesting example which used state machine to implement greedy snake game 	controller. The following diagram shows that the state machine definition of the controller.   
	![SnakeStateMachine](http://hekailiang.github.io/squirrel/images/SnakeGame.png)  
	This example can be found in package *"org.squirrelframework.foundation.fsm.snake"*. 

* **Spring Framework Integration**  
Squirrel state machine does not have any heavy dependencies, so basically it should be highly embedable. To Integrate with Spring IoC container, basically user can add @Configurable annotation on the state machine implementation class, e.g.
	```java
	interface StateMachineBean extends StateMachine<StateMachineBean, MyState, MyEvent, MyContext> {
		...
	}

	@Configurable(preConstruction=true)
	abstract class AbstractStateMachineBean extends AbstractStateMachine<StateMachineBean, MyState, MyEvent, MyContext> implements StateMachineBean {
		@Autowired
  		private ApplicationContext applicationContext;
		...
	}
	
	public class TypedStateMachineA extends AbstractStateMachineBean {
  		@Autowired
  		// some other managed beans...
	}

	public class TypedStateMachineB extends AbstractStateMachineBean {
  		@Autowired
  		// some other managed beans...
	}
	
	TypedStateMachineA fsmA = StateMachineBuilderFactory.create(TypedStateMachineA.class, 
		MyState.class, MyEvent.class, MyContext.class).newStateMachine(MyState.Initial);
	TypedStateMachineA fsmB = StateMachineBuilderFactory.create(TypedStateMachineB.class, 
		MyState.class, MyEvent.class, MyContext.class).newStateMachine(MyState.Initial);
	```  

* **Andriod Integration**   
Thanks to [Vyacheslav Blinov](https://github.com/dant3) to provide this [sample project](https://github.com/dant3/squirrel-android-example) for squirrel state machine used in android.

* **Use Cases**  
[Why Choose Squirrel (Chinese)](http://www.timguan.net/2017/06/19/%E7%8A%B6%E6%80%81%E6%9C%BA%E5%BC%95%E6%93%8E%E9%80%89%E5%9E%8B/)  
[squirrel-foundation Use Details (Chinese)](http://www.timguan.net/2017/06/21/squirrel-foundation%E7%8A%B6%E6%80%81%E6%9C%BA%E7%9A%84%E4%BD%BF%E7%94%A8%E7%BB%86%E8%8A%82/#more)  
[Finite-State Machine for Single-Use Code Authentication (English)](http://www.ebaytechblog.com/2016/08/30/finite-state-machine-for-single-use-code-authentication/)  
