package org.squirrelframework.foundation.component;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;

import java.util.List;

import org.junit.Test;
import org.squirrelframework.foundation.component.SquirrelPostProcessor;
import org.squirrelframework.foundation.component.SquirrelPostProcessorProvider;
import org.squirrelframework.foundation.component.SquirrelProvider;

public class ComponentPostProcessorTest {
    
    @Test
    public void testRegisterPostProcessor() {
        SquirrelPostProcessorProvider.getInstance().register(Person.class, new SquirrelPostProcessor<Person>() {
            @Override
            public void postProcess(Person p) {
                p.setName("Henry");
            }
        });
        
        Person p = SquirrelProvider.getInstance().newInstance(Person.class);
        assertThat(p, notNullValue());
        assertThat(p, instanceOf(PersonImpl.class));
        assertThat(p.getName(), equalTo("Henry"));
        
        SquirrelPostProcessorProvider.getInstance().unregister(Person.class);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testAssignablePostProcessor() {
        SquirrelPostProcessor<Object> pp = new SquirrelPostProcessor<Object>() {
            @Override
            public void postProcess(Object p) {}
        };
        SquirrelPostProcessorProvider.getInstance().register(Person.class, pp);
        List<SquirrelPostProcessor<? super Student>> studentPostProcessors = SquirrelPostProcessorProvider.
                getInstance().getCallablePostProcessors(Student.class);
        assertThat((SquirrelPostProcessor<Object>)studentPostProcessors.get(0), sameInstance(pp));
        SquirrelPostProcessorProvider.getInstance().unregister(Person.class);
        
        SquirrelPostProcessorProvider.getInstance().register(Student.class, pp);
        List<SquirrelPostProcessor<? super Person>> personPostProcessors = SquirrelPostProcessorProvider.
                getInstance().getCallablePostProcessors(Person.class);
        assertThat(personPostProcessors, empty());
        SquirrelPostProcessorProvider.getInstance().unregister(Student.class);
        
        SquirrelPostProcessorProvider.getInstance().register(Person.class, pp);
        SquirrelPostProcessorProvider.getInstance().register(Student.class, pp);
        List<SquirrelPostProcessor<? super Student>> studentAndPersonPostProcessors = SquirrelPostProcessorProvider.
                getInstance().getCallablePostProcessors(Student.class);
        assertThat((SquirrelPostProcessor<Object>)studentAndPersonPostProcessors.get(0), sameInstance(pp));
        assertThat((SquirrelPostProcessor<Object>)studentAndPersonPostProcessors.get(1), sameInstance(pp));
        SquirrelPostProcessorProvider.getInstance().unregister(Person.class);
        SquirrelPostProcessorProvider.getInstance().unregister(Student.class);
        
    }
    
    @Test
    public void testCompositePostProssor() {
        SquirrelPostProcessor<Person> p1 = new SquirrelPostProcessor<Person>() {
            @Override
            public void postProcess(Person p) {
                p.setName("Henry");
            }
        };
        SquirrelPostProcessor<Student> p2 = new SquirrelPostProcessor<Student>() {
            @Override
            public void postProcess(Student p) {
                p.setSchool("XJTU");
            }
        };
        SquirrelPostProcessorProvider.getInstance().register(Student.class, p1);
        SquirrelPostProcessorProvider.getInstance().register(Student.class, p2);
        
        List<SquirrelPostProcessor<? super Student>> studentPostProcessors = SquirrelPostProcessorProvider.
                getInstance().getCallablePostProcessors(Student.class);
        assertThat(studentPostProcessors.size(), is(1));
        
        Student student = SquirrelProvider.getInstance().newInstance(Student.class);
        assertThat(student.getName(), equalTo("Henry"));
        assertThat(student.getSchool(), equalTo("XJTU"));
        
        SquirrelPostProcessorProvider.getInstance().unregister(Student.class);
        SquirrelPostProcessorProvider.getInstance().unregister(Student.class);
    }
}
