package org.squirrelframework.foundation.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squirrelframework.foundation.event.ListenTarget;
import org.squirrelframework.foundation.exception.ErrorCodes;
import org.squirrelframework.foundation.exception.SquirrelRuntimeException;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class ReflectUtils {

    private ReflectUtils() {}

    private final static Logger logger = LoggerFactory.getLogger(ReflectUtils.class);

    public static Set<Field> getAllDeclaredFields(final Class<?> theClass) {
        Set<Field> aFields = Sets.newHashSet();
        Class<?> aClass = theClass;
        while (aClass != null) {
            aFields.addAll(Sets.newHashSet(aClass.getDeclaredFields()));
            aClass = aClass.getSuperclass();
        }
        return aFields;
    }

    /** searches for the field in the given class and in its super classes */
    public static Field getField(Class<?> clazz, String fieldName) {
        return getField(clazz, fieldName, clazz);
    }

    private static Field getField(Class<?> clazz, String fieldName, Class<?> original) {
        Field field = null;
        try {
            field = clazz.getDeclaredField(fieldName);
            if (logger.isTraceEnabled()) {
                logger.trace("found field "+fieldName+" in "+clazz.getName());
            }
        } catch (SecurityException e) {
            throw new SquirrelRuntimeException(e, ErrorCodes.NOT_ALLOW_ACCESS_FIELD, clazz.getName(), fieldName);
        } catch (NoSuchFieldException e) {
            if (clazz.getSuperclass()!=null) {
                return getField(clazz.getSuperclass(), fieldName, original);
            } else {
                throw new SquirrelRuntimeException(e, ErrorCodes.FIELD_NOT_FOUND, original.getName(), fieldName);
            }
        }
        return field;
    }

    /** searches for the method in the given class and in its super classes */
    public static Method getMethod(Class<?> clazz, String methodName, Class<?>[] parameterTypes) {
        return getMethod(clazz, methodName, parameterTypes, clazz);
    }

    private static Method getMethod(Class<?> clazz, String methodName, Class<?>[] parameterTypes, Class<?> original) {
        Method method = null;
        try {
            method = clazz.getDeclaredMethod(methodName, parameterTypes);
            if (logger.isTraceEnabled()) {
                logger.trace("found method "+clazz.getName()+"."+methodName+"("+Arrays.toString(parameterTypes)+")");
            }
        } catch (SecurityException e) {
            throw new SquirrelRuntimeException(e, ErrorCodes.NOT_ALLOW_ACCESS_METHOD, clazz.getName(), methodName, getParameterTypesText(parameterTypes));
        } catch (NoSuchMethodException e) {
            if (clazz.getSuperclass()!=null) {
                return getMethod(clazz.getSuperclass(), methodName, parameterTypes, original);
            } else {
                throw new SquirrelRuntimeException(e, ErrorCodes.METHOD_NOT_FOUND, original.getName(), methodName, getParameterTypesText(parameterTypes));
            }
        }
        return method;
    }

    private static String getParameterTypesText(Class<?>[] parameterTypes) {
        if (parameterTypes==null) return "";
        StringBuilder parameterTypesText = new StringBuilder();
        for (int i=0; i<parameterTypes.length; i++) {
            Class<?> parameterType = parameterTypes[i];
            parameterTypesText.append(parameterType.getName());
            if (i!=parameterTypes.length-1) {
                parameterTypesText.append(", ");
            }
        }
        return parameterTypesText.toString();
    }
    
    public static String logMethod(Method method) {
        StringBuilder builder = new StringBuilder(method.getDeclaringClass().getSimpleName());
        builder.append('.').append(method.getName()).append('(');
        if(method.getParameterTypes()!=null) {
            for(int i=0, size=method.getParameterTypes().length; i<size; ++i) {
                if(i!=0) builder.append(", ");
                builder.append(method.getParameterTypes()[i].getSimpleName());
            }
        }
        builder.append(')');
        return builder.toString();
    }

    /**
     * Return the given annotation from the class.  If the class does not have the annotation, it's parent class and any
     * interfaces will also be checked.
     * @param theClass the class to inspect
     * @param theAnnotation the annotation to retrieve
     * @return the class's annotation, or it's "inherited" annotation, or null if the annotation cannot be found.
     */
    public static <T extends Annotation> T getAnnotation(Class<?> theClass, Class<T> theAnnotation) {
        T aAnnotation = null;

        if (theClass.isAnnotationPresent(theAnnotation)) {
            aAnnotation = theClass.getAnnotation(theAnnotation);
        } else {
            if (shouldInspectClass(theClass.getSuperclass())) 
                aAnnotation = getAnnotation(theClass.getSuperclass(), theAnnotation);

            if (aAnnotation == null) {
                for (Class<?> aInt : theClass.getInterfaces()) {
                    aAnnotation = getAnnotation(aInt, theAnnotation);
                    if (aAnnotation != null) { break; }
                }
            }
        }
        return aAnnotation;
    }

    /**
     * Return whether or not the class has the given annotation.  If the class itself does not have the annotation,
     * it's super class and the interfaces it implements are checked.
     * @param theClass the class to check
     * @param theAnnotation the annotation to look for
     * @return if the class has the annotation, or one of its parents does and it "inherited" the annotation, false otherwise
     */
    public static boolean hasAnnotation(Class<?> theClass, Class<? extends Annotation> theAnnotation) {
        return getAnnotation(theClass, theAnnotation) != null;
    }

    public static List<Method> getAnnotatedMethods(
            Class<?> targetClass, Class<? extends Annotation> annotationClass) {
        List<Method> aMethods = Lists.newArrayList();

        for (Method aMethod : targetClass.getMethods()) {
            if (aMethod.getAnnotation(annotationClass) != null) {
                aMethods.add(aMethod);
            }
        }

        if (shouldInspectClass(targetClass.getSuperclass())) {
            aMethods.addAll(getAnnotatedMethods(targetClass.getSuperclass(), annotationClass));
        }

        for (Class<?> aInterface : targetClass.getInterfaces()) {
            aMethods.addAll(getAnnotatedMethods(aInterface, annotationClass));
        }

        return aMethods;
    }

    private static boolean shouldInspectClass(final Class<?> theClass) {
        return !Object.class.equals(theClass) && theClass != null;
    }

    public static Field[] getAnnotatedFields(
            Class<?> targetClass, Class<? extends Annotation> annotationClass) {
        List<Field> annotatedFields = Lists.newArrayList();
        Class<?> k = targetClass;
        while(shouldInspectClass(k)) {
            for(Field f : k.getFields()) {
                if(f.getAnnotation(annotationClass)!=null) {
                    annotatedFields.add(f);
                }
            }
            k = k.getSuperclass();
        }
        return annotatedFields.toArray(new Field[0]);
    }

    public static boolean isAnnotatedWith(Object obj, Class<? extends Annotation> theAnnotation) {
        if(obj instanceof Class) {
            return hasAnnotation((Class<?>)obj, theAnnotation);
        } else if(obj instanceof Field) {
            return ((Field)obj).getAnnotation(theAnnotation)!=null;
        } else if(obj instanceof Method) {
            return ((Method)obj).getAnnotation(theAnnotation)!=null;
        } // TODO-hhe: how about annotation with parameter?
        return false;
    }

    public static <T> List<T> getListenTargets(Object obj, Class<?> listenerType, Class<T> container) {
        Preconditions.checkNotNull(obj!=null);
        List<T> result = Lists.newArrayList();

        Field[] fields = getAnnotatedFields(obj.getClass(), ListenTarget.class);
        for(Field f : fields) {
            ListenTarget annotation = f.getAnnotation(ListenTarget.class);
            if(annotation.value().isAssignableFrom(listenerType)) {
                try {
                    Object listenTarget = f.get(obj);
                    if(listenTarget!=null && container.isAssignableFrom(listenTarget.getClass())) {
                        result.add(container.cast(listenTarget));
                    }
                } catch (Exception e) {} 
            }
        }

        List<Method> methods = getAnnotatedMethods(obj.getClass(), ListenTarget.class);
        for(Method m : methods) {
            ListenTarget annotation = m.getAnnotation(ListenTarget.class);
            if(annotation.value().isAssignableFrom(listenerType)) {
                try {
                    Object listenTarget = m.invoke(obj, new Object[]{});
                    if(listenTarget!=null && container.isAssignableFrom(listenTarget.getClass())) {
                        result.add(container.cast(listenTarget));
                    }
                } catch (Exception e) {} 
            }
        }
        return result;
    }

    public static Method getFirstMethodOfName(Class<?> clazz, String name) {
        for(Method m : clazz.getMethods()) 
            if(m.getName().equals(name)) { return m; }
        return null;
    }

    public static <T> Constructor<T> getConstructor(Class<T> type, Class<?>[] parameterTypes) {
        try {
            Constructor<T> constructor = type.getDeclaredConstructor(parameterTypes);
            return constructor;
        } catch (NoSuchMethodException e) {
            throw new SquirrelRuntimeException(e, ErrorCodes.CONSTRUCTOR_NOT_FOUND, 
                    type.getName(), Arrays.toString(parameterTypes));
        }
    }

    public static <T> T newInstance(Class<T> clazz) {
        return newInstance(clazz, null, null);
    }

    public static <T> T newInstance(Constructor<T> constructor) {
        return newInstance(null, constructor, null);
    }

    public static <T> T newInstance(Constructor<T> constructor, Object[] args) {
        return newInstance(null, constructor, args);
    }

    private static <T> T newInstance(Class<T> clazz, Constructor<T> constructor, Object[] args) {
        if ( (clazz==null) && (constructor==null) ) {
            throw new IllegalArgumentException("can't create new instance without clazz or constructor");
        }

        if (logger.isTraceEnabled()) {
            logger.trace("creating new instance for class '"+clazz.getName()+"' with args "+Arrays.toString(args));
        }
        if (constructor==null) {
            if (logger.isTraceEnabled()) logger.trace("getting default constructor");
            constructor = getConstructor(clazz, (Class[])null);
        }

        boolean oldAccessible = constructor.isAccessible();
        try {

            if (!constructor.isAccessible()) {
                if (logger.isTraceEnabled()) logger.trace("making constructor accessible");
                constructor.setAccessible(true);
            }
            return constructor.newInstance(args);

        } catch (Throwable t) {
            throw new SquirrelRuntimeException(t, ErrorCodes.CONSTRUCT_NEW_INSTANCE_ERROR, clazz.getName(), Arrays.toString(args));
        } finally {
            constructor.setAccessible(oldAccessible);
        }
    }

    public static Object getStatic(Field field) {
        return get(field, null);
    }

    public static Object get(Field field, Object object) {
        if (field==null) {
            throw new SquirrelRuntimeException(ErrorCodes.FIELD_NULL);
        }
        boolean oldAccessible = field.isAccessible();
        try {
            field.setAccessible(true);
            Object value = field.get(object);
            if (logger.isTraceEnabled()) {
                logger.trace("got value '"+value+"' from field '"+field.getName()+"'");
            }
            return value;
        } catch (Exception e) {
            throw new SquirrelRuntimeException(e, ErrorCodes.CANNOT_GET_FIELD_VALUE, field.getName());
        } finally {
            field.setAccessible(oldAccessible);
        }
    }

    public static void setStatic(Field field, Object value) {
        set(field, null, value);
    }

    public static void set(Field field, Object object, Object value) {
        if (field==null) {
            throw new SquirrelRuntimeException(ErrorCodes.FIELD_NULL);
        }
        boolean oldAccessible = field.isAccessible();
        try {
            if (logger.isTraceEnabled()) {
                logger.trace("setting field '"+field.getName()+"' to value '"+value+"'");
            }
            if (!oldAccessible) {
                if (logger.isTraceEnabled()) logger.trace("making field accessible");
                field.setAccessible(true);
            }
            field.set(object, value);
        } catch (Exception e) {
            throw new SquirrelRuntimeException(e, ErrorCodes.CANNOT_SET_FIELD_VALUE, field.getName(), value);
        } finally {
            field.setAccessible(oldAccessible);
        }
    }

    public static Object invokeStatic(Method method) {
        return invoke(method, null, new Object[0]);
    }

    public static Object invokeStatic(Method method, Object[] args) {
        return invoke(method, null, args);
    }

    public static Object invoke(Method method, Object target) {
        return invoke(method, target, new Object[0]);
    }

    public static Object invoke(Method method, Object target, Object[] args) {
        if (method==null) {
            throw new SquirrelRuntimeException(ErrorCodes.METHOD_NULL);
        }
        boolean oldAccessible = method.isAccessible();
        try {
            if (logger.isTraceEnabled()) {
                logger.trace("invoking '"+method.getName()+"' on '"+target+"' with "+Arrays.toString(args));
            }
            if (!method.isAccessible()) {
                logger.trace("making method accessible");
                method.setAccessible(true);
            }
            return method.invoke(target, args);
        } catch (InvocationTargetException e) {
            Throwable targetException = e.getTargetException();
            throw new SquirrelRuntimeException(targetException, ErrorCodes.METHOD_INVOKE_ERROR, 
                    method, Arrays.toString(args), target, targetException.getCause());
        } catch (Exception e) {
            throw new SquirrelRuntimeException(e, ErrorCodes.METHOD_INVOKE_ERROR, 
                    method, Arrays.toString(args), target, e.getMessage());
        } finally {
            method.setAccessible(oldAccessible);
        }
    }
    
    /**
     * Invoke the given callback on all fields in the target class, going up the
     * class hierarchy to get all declared fields.
     * @param clazz the target class to analyze
     * @param fc the callback to invoke for each field
     */
    public static void doWithFields(Class<?> clazz, FieldCallback fc) throws IllegalArgumentException {
        doWithFields(clazz, fc, null);
    }

    /**
     * Invoke the given callback on all fields in the target class, going up the
     * class hierarchy to get all declared fields.
     * @param clazz the target class to analyze
     * @param fc the callback to invoke for each field
     * @param ff the filter that determines the fields to apply the callback to
     */
    public static void doWithFields(Class<?> clazz, FieldCallback fc, FieldFilter ff)
            throws IllegalArgumentException {

        // Keep backing up the inheritance hierarchy.
        Class<?> targetClass = clazz;
        do {
            Field[] fields = targetClass.getDeclaredFields();
            for (Field field : fields) {
                // Skip static and final fields.
                if (ff != null && !ff.matches(field)) {
                    continue;
                }
                fc.doWith(field);
            }
            targetClass = targetClass.getSuperclass();
        }
        while (targetClass != null && targetClass != Object.class);
    }
    
    /**
     * Perform the given callback operation on all matching methods of the given
     * class and superclasses.
     * <p>The same named method occurring on subclass and superclass will appear
     * twice, unless excluded by a {@link MethodFilter}.
     * @param clazz class to start looking at
     * @param mc the callback to invoke for each method
     * @see #doWithMethods(Class, MethodCallback, MethodFilter)
     */
    public static void doWithMethods(Class<?> clazz, MethodCallback mc) throws IllegalArgumentException {
        doWithMethods(clazz, mc, null);
    }

    /**
     * Perform the given callback operation on all matching methods of the given
     * class and superclasses (or given interface and super-interfaces).
     * <p>The same named method occurring on subclass and superclass will appear
     * twice, unless excluded by the specified {@link MethodFilter}.
     * @param clazz class to start looking at
     * @param mc the callback to invoke for each method
     * @param mf the filter that determines the methods to apply the callback to
     */
    public static void doWithMethods(Class<?> clazz, MethodCallback mc, MethodFilter mf)
            throws IllegalArgumentException {

        // Keep backing up the inheritance hierarchy.
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (mf != null && !mf.matches(method)) {
                continue;
            }
            mc.doWith(method);
        }
        if (clazz.getSuperclass() != null) {
            doWithMethods(clazz.getSuperclass(), mc, mf);
        }
        else if (clazz.isInterface()) {
            for (Class<?> superIfc : clazz.getInterfaces()) {
                doWithMethods(superIfc, mc, mf);
            }
        }
    }

    /**
     * Returns all superclasses.
     */
    public static Class<?>[] getSuperclasses(Class<?> type) {
        int i = 0;
        for (Class<?> x = type.getSuperclass(); x != null; x = x.getSuperclass()) {
            i++;
        }
        Class<?>[] result = new Class[i];
        i = 0;
        for (Class<?> x = type.getSuperclass(); x != null; x = x.getSuperclass()) {
            result[i] = x;
            i++;
        }
        return result;
    }

    /**
     * Returns <code>true</code> if method is user defined and not defined in <code>Object</code> class.
     */
    public static boolean isUserDefinedMethod(final Method method) {
        return method.getDeclaringClass() != Object.class;
    }


    /**
     * Returns <code>true</code> if method is a bean property.
     */
    public static boolean isBeanProperty(Method method) {
        String methodName = method.getName();
        Class<?> returnType = method.getReturnType();
        Class<?>[] paramTypes =  method.getParameterTypes();
        if (methodName.startsWith("get") && methodName.equals("getClass") == false) {    // getter method must starts with 'get' and it is not getClass()
            if ((returnType != null) && (paramTypes.length == 0)) {  // getter must have a return type and no arguments
                return true;
            }
        } else if (methodName.startsWith("is")) {    // ister must starts with 'is'
            if ((returnType != null)  && (paramTypes.length == 0)) {  // ister must have return type and no arguments
                return true;
            }
        } else if (methodName.startsWith("set")) {    // setter must start with a 'set'
            if (paramTypes.length == 1) {        // setter must have just one argument
                return true;
            }
        }
        return false;
    }

    public static String getPackageName(final String className) {
        if (className==null || className.length() == 0)
            throw new SquirrelRuntimeException(ErrorCodes.ILLEGAL_CLASS_NAME);

        int index = className.lastIndexOf('.');
        if (index != -1)
            return className.substring(0, index);
        return "";
    }

    public static Class<?> getClass(final String className) {
        if (className==null || className.length() == 0)
            throw new SquirrelRuntimeException(ErrorCodes.ILLEGAL_CLASS_NAME);
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new SquirrelRuntimeException(e, ErrorCodes.CLASS_NOT_FOUND, className);
        }
    }
    
    /**
     * Action to take on each method.
     */
    public interface MethodCallback {

        /**
         * Perform an operation using the given method.
         * @param method the method to operate on
         */
        void doWith(Method method);
    }


    /**
     * Callback optionally used to filter methods to be operated on by a method callback.
     */
    public interface MethodFilter {

        /**
         * Determine whether the given method matches.
         * @param method the method to check
         */
        boolean matches(Method method);
    }
    
    /**
     * Callback interface invoked on each field in the hierarchy.
     */
    public interface FieldCallback {

        /**
         * Perform an operation using the given field.
         * @param field the field to operate on
         */
        void doWith(Field field);
    }


    /**
     * Callback optionally used to filter fields to be operated on by a field callback.
     */
    public interface FieldFilter {

        /**
         * Determine whether the given field matches.
         * @param field the field to check
         */
        boolean matches(Field field);
    }
}
