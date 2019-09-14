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
Squirrel state machine does not have any heavy dependencies, so basically it should be highly embedable. To Integrate with Spring IoC container, basically user can register an statemachine post processor to enable auto wire dependencies. Following StateMachineAutowireProcessor provide the sample implementation.
	```java
	package org.squirreframework.foundation.spring;

	import com.google.common.base.Preconditions;
	import org.springframework.beans.BeansException;
	import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
	import org.springframework.context.ApplicationContext;
	import org.springframework.context.ApplicationContextAware;
	import org.springframework.stereotype.Component;
	import org.squirrelframework.foundation.component.SquirrelPostProcessor;
	import org.squirrelframework.foundation.component.SquirrelPostProcessorProvider;
	import org.squirrelframework.foundation.fsm.*;

	/**
	 * Support autowire dependencies within spring IoC container
	 *
	 * @author kailiang.hkl
	 * @version : StateMachineAutowireProcessor.java
	 */
	@Component
	public class StateMachineAutowireProcessor implements SquirrelPostProcessor<StateMachine>, ApplicationContextAware {

	    private ApplicationContext applicationContext;

	    public StateMachineAutowireProcessor() {
	        // register StateMachineAutowireProcessor as state machine post processor
		SquirrelPostProcessorProvider.getInstance().register(StateMachine.class, this);
	    }

	    @Override
	    public void postProcess(StateMachine stateMachine) {
		Preconditions.checkNotNull(stateMachine);
		// after state machine instance created, 
		// autoware @Autowired/@Value dependencies and properties within state machine class
		AutowireCapableBeanFactory autowireCapableBeanFactory = applicationContext.getAutowireCapableBeanFactory();
		autowireCapableBeanFactory.autowireBean(stateMachine);
	    }

	    @Override
	    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	    }
	}
	```  

* **Andriod Integration**   
Thanks to [Vyacheslav Blinov](https://github.com/dant3) to provide this [sample project](https://github.com/dant3/squirrel-android-example) for squirrel state machine used in android.

* **Use Cases**  
The following links linked to some good articles about how to use squirrel state machine library. If the author does not like to be referred here, please contact me. I will withdraw immediately.  
	* [Why Choose Squirrel (Chinese)](http://www.timguan.net/2017/06/19/%E7%8A%B6%E6%80%81%E6%9C%BA%E5%BC%95%E6%93%8E%E9%80%89%E5%9E%8B/)  
	* [squirrel-foundation Use Details (Chinese)](https://segmentfault.com/a/1190000009906469)   
	* [Finite-State Machine for Single-Use Code Authentication (English)](http://www.ebaytechblog.com/2016/08/30/finite-state-machine-for-single-use-code-authentication/)   
	* [Squirrel Usages (Chinese)](http://www.yangguo.info/2015/02/01/squirrel/)  
	* [State Machines Comparison (Chinese)](http://www.jianshu.com/p/3ca1ff2d7344)  
