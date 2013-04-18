package org.squirrelframework.foundation.component;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import org.squirrelframework.foundation.util.ReflectUtils;
import org.squirrelframework.foundation.util.TypeReference;

/**
 * Central factory class for components used by squirrel-foundation.
 * 
 * @author Henry.He
 *
 */
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
    
    public <T> T newInstance(TypeReference<T> typeRef, Class<?>[] argTypes, Object[] args) {
        Class<T> clz = typeRef.getRawType();
        return clz.cast(newInstance(clz, argTypes, args));
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
     * If class has register a implementation class, return register implementation class. If register class or implement 
     * class is an interface, try to find corresponding implementation class over naming convention. 
     * (implementation class simple name = interface class simple name + "Impl") First try to find the implementation class 
     * with conventional naming under the same package as interface class. If still not exist, try to find implementation class 
     * in (interface class package + ".impl").
     * 
     * @param clz registered class
     * @return current registered implementation
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
