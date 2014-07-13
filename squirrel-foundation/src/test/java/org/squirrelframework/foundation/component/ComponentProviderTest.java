package org.squirrelframework.foundation.component;

import org.junit.After;
import org.junit.Test;
import org.squirrelframework.foundation.component.impl.ProgrammerImpl;
import org.squirrelframework.foundation.component.impl.StudentImpl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ComponentProviderTest {

    @After
    public void tearDown() {
        SquirrelProvider.getInstance().clearRegistry();
    }
    
    @Test
    public void testNewInstance() {
        Person p = SquirrelProvider.getInstance().newInstance(PersonImpl.class);
        assertThat(p, notNullValue());
        assertThat(p, instanceOf(PersonImpl.class));
    }
    
    @Test
    public void testNewInstanceWithArg() {
        Person p = SquirrelProvider.getInstance().newInstance(PersonImpl.class, new Class[]{String.class}, new Object[]{"Henry"});
        assertThat(p, notNullValue());
        assertThat(p, instanceOf(PersonImpl.class));
        assertThat(p.getName(), equalTo("Henry"));
    }
    
    @Test
    public void testNewInterface() {
        Person p = SquirrelProvider.getInstance().newInstance(Person.class);
        assertThat(p, notNullValue());
        assertThat(p, instanceOf(PersonImpl.class));
    }
    
    @Test
    public void testOverrideImplClass() {
        SquirrelProvider.getInstance().register(PersonImpl.class, StudentImpl.class);
        
        Person p = SquirrelProvider.getInstance().newInstance(Person.class);
        assertThat(p, notNullValue());
        assertThat(p, instanceOf(PersonImpl.class));
        
        Person p2 = SquirrelProvider.getInstance().newInstance(PersonImpl.class);
        assertThat(p2, notNullValue());
        assertThat(p2, instanceOf(StudentImpl.class));
        
        SquirrelProvider.getInstance().unregister(PersonImpl.class);
    }
    
    @Test
    public void testNewInterfaceWithArg() {
        Person p = SquirrelProvider.getInstance().newInstance(Person.class, new Class[]{String.class}, new Object[]{"Henry"});
        assertThat(p, notNullValue());
        assertThat(p, instanceOf(PersonImpl.class));
        assertThat(p.getName(), equalTo("Henry"));
    }
    
    @Test
    public void testNewInterfaceWithImpl() {
        Student p = SquirrelProvider.getInstance().newInstance(Student.class);
        assertThat(p, notNullValue());
        assertThat(p, instanceOf(StudentImpl.class));
    }
    
    @Test
    public void testRegisterAnotherImpl() {
        SquirrelProvider.getInstance().register(Person.class, StudentImpl.class);
        Person p = SquirrelProvider.getInstance().newInstance(Person.class);
        assertThat(p, notNullValue());
        assertThat(p, instanceOf(StudentImpl.class));
        SquirrelProvider.getInstance().unregister(Person.class);
    }
    
    @Test
    public void testRegisterAnotherInterface() {
        SquirrelProvider.getInstance().register(Person.class, Student.class);
        Person p = SquirrelProvider.getInstance().newInstance(Person.class);
        assertThat(p, notNullValue());
        assertThat(p, instanceOf(StudentImpl.class));
        SquirrelProvider.getInstance().unregister(Person.class);
    }

    @Test
    public void testImplUnregistrationRevertsToUsingDefaultImpl() {
        SquirrelProvider.getInstance().register(Person.class, StudentImpl.class);
        Person p = SquirrelProvider.getInstance().newInstance(Person.class);
        assertThat(p, notNullValue());
        assertThat(p, instanceOf(StudentImpl.class));

        SquirrelProvider.getInstance().unregister(Person.class);
        p = SquirrelProvider.getInstance().newInstance(Person.class);
        assertThat(p, notNullValue());
        assertThat(p, instanceOf(PersonImpl.class));
    }

    @Test
    public void testInterfaceUnregistrationRevertsToUsingDefaultImpl() {
        SquirrelProvider.getInstance().register(Person.class, Student.class);
        Person p = SquirrelProvider.getInstance().newInstance(Person.class);
        assertThat(p, notNullValue());
        assertThat(p, instanceOf(StudentImpl.class));

        SquirrelProvider.getInstance().unregister(Person.class);
        p = SquirrelProvider.getInstance().newInstance(Person.class);
        assertThat(p, notNullValue());
        assertThat(p, instanceOf(PersonImpl.class));
    }

    /**
     * I would not think this should be a supported use case but nevertheless it
     * failed because it only performed one additional lookup iteration on
     * finding an interface...
     */
    @Test
    public void testRegisterInterfaceToInterface() {
        SquirrelProvider.getInstance().register(Person.class, Student.class);
        SquirrelProvider.getInstance().register(Student.class, Programmer.class);
        Person p = SquirrelProvider.getInstance().newInstance(Person.class);
        assertThat(p, notNullValue());
        assertThat(p, instanceOf(ProgrammerImpl.class));
        SquirrelProvider.getInstance().unregister(Person.class);
    }

    /**
     * Checks for cycles (maybe better to detect at registration time?) - should not get stack overflow error
     */
    @Test(expected = IllegalStateException.class)
    public void testRegisterInterfaceToSameInterfaceDoesntRecurse() {
        SquirrelProvider.getInstance().register(Person.class, Student.class);
        SquirrelProvider.getInstance().register(Student.class, Person.class);

        SquirrelProvider.getInstance().newInstance(Person.class);
    }
}
