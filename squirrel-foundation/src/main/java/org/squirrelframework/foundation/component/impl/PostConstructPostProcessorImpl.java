package org.squirrelframework.foundation.component.impl;

import java.lang.reflect.Method;

import org.squirrelframework.foundation.component.SquirrelPostProcessor;
import org.squirrelframework.foundation.util.ReflectUtils;

/**
 * For the component instantiated by component provider, the method annotated with PostConstruct will
 * be invoked after component being created.
 * 
 * @author Henry.He
 *
 */
public class PostConstructPostProcessorImpl implements SquirrelPostProcessor<Object> {
    
    @Override
    public void postProcess(final Object component) {
        final Class<?> componentClass = component.getClass();
        ReflectUtils.doWithMethods(componentClass, new ReflectUtils.MethodCallback() {
            @Override
            public void doWith(Method method) {
                ReflectUtils.invoke(method, component);
            }
        }, new ReflectUtils.MethodFilter() {
            @Override
            public boolean matches(Method method) {
                return ReflectUtils.isAnnotatedWith(method, javax.annotation.PostConstruct.class) && 
                        (method.getParameterTypes()==null || method.getParameterTypes().length==0);
            }
        });
    }
}
