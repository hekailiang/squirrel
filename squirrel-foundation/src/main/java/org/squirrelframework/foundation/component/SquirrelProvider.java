package org.squirrelframework.foundation.component;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.squirrelframework.foundation.exception.ErrorCodes;
import org.squirrelframework.foundation.exception.SquirrelRuntimeException;
import org.squirrelframework.foundation.util.ReflectUtils;
import org.squirrelframework.foundation.util.TypeReference;

public class SquirrelProvider implements SquirrelSingleton {

    private static SquirrelProvider instance = new SquirrelProvider();

    public static SquirrelProvider getInstance() {
        return instance;
    }

    public static void setInstance(SquirrelProvider instance) {
        SquirrelProvider.instance = instance;
    }

    private Map<Class<?>, Class<?>> implementationRegistry = new HashMap<Class<?>, Class<?>>();

    public <T> T newInstance(TypeReference<T> typeRef) {
        return newInstance(typeRef, null, null);
    }
    
    @SuppressWarnings("unchecked")
    public <T> T newInstance(TypeReference<T> typeRef, Class<?>[] argTypes, Object[] args) {
        Class<?> clz = null;
        Type type = typeRef.getType();
        if (type instanceof Class<?>) {
            clz = (Class<?>)type;
        } else if(type instanceof ParameterizedType) {
            Type rawType = ((ParameterizedType) type).getRawType();
            clz = (Class<?>)rawType;
        } else {
            throw new SquirrelRuntimeException(ErrorCodes.UNSUPPORTED_TYPE_REFERENCE, typeRef.getType());
        }
        return (T) newInstance(clz, argTypes, args);
    }
    
    /**
     * Create a new instance of the requested class using the internal registry.
     */
    public <T> T newInstance(Class<T> clz) {
        return newInstance(clz, null, null);
    }
    
    /**
     * Create a new instance of the requested class using the internal registry.
     */
    public <T> T newInstance(Class<T> clz, Class<?>[] argTypes, Object[] args) {
        Class<T> implementationClass = getImplementation(clz);
        if (args == null) {
            return postProcess(clz, ReflectUtils.newInstance(implementationClass));
        }
        Constructor<T> constructor = ReflectUtils.getConstructor(implementationClass, argTypes);
        return postProcess(clz, ReflectUtils.newInstance(constructor, args));
    }
    
    private <T> T postProcess(Class<T> clz, T component) {
        SquirrelPostProcessor<T> postProcessor = 
                SquirrelPostProcessorProvider.getInstance().getPostProcessor(clz);
        if(postProcessor!=null && component!=null) {
            postProcessor.postProcess(component);
        }
        return component;
    }

    /**
     * Register the implementation class for a certain class. Note, if there is already an entry in the registry for
     * the class, then it will be overwritten.
     */
    public void register(Class<?> clazz, Class<?> implementationClass) {
        // TODO: handle the case that there is already an entry...
        implementationRegistry.put(clazz, implementationClass);
    }
    
    public void unregister(Class<?> clazz) {
        implementationRegistry.remove(clazz);
    }
    
    public void clearRegistry() {
        implementationRegistry.clear();
    }

    /**
     * Return the current registered implementation.
     */
    @SuppressWarnings("unchecked")
    public <T> Class<T> getImplementation(Class<T> clz) {
        Class<?> implementationClass = implementationRegistry.get(clz);
        if(implementationClass==null) {
            if(clz.isInterface()) {
                implementationClass = findImplementationClass(clz);
            } else {
                implementationClass = clz;
            }
        } else if(implementationClass.isInterface()) {
            implementationClass = findImplementationClass(implementationClass);
        }
        return (Class<T>)implementationClass;
    }
    
    // find implementation class name according to programming convention
    private Class<?> findImplementationClass(Class<?> interfaceClass) {
        Class<?> implementationClass = null;
        String implClassName = interfaceClass.getName()+"Impl";
        try {
            implementationClass = Class.forName(implClassName);
        } catch (ClassNotFoundException e) {
            implClassName = ReflectUtils.getPackageName(interfaceClass.getName())+".impl."+interfaceClass.getSimpleName()+"Impl";
            implementationClass = ReflectUtils.getClass(implClassName);
        }
        return implementationClass;
    }
}
