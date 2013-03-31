package org.squirrel.foundation.fsm.impl;

import java.util.Map;

import org.squirrel.foundation.component.SquirrelComponent;
import org.squirrel.foundation.component.SquirrelProvider;
import org.squirrel.foundation.component.SquirrelSingleton;
import org.squirrel.foundation.fsm.Converter;

import com.google.common.collect.Maps;

public class ConverterProvider implements SquirrelComponent, SquirrelSingleton {
    
    private static ConverterProvider instance = SquirrelProvider.getInstance().newInstance(ConverterProvider.class);
    
    public static ConverterProvider getInstance() {
        return instance;
    }
    
    public static void setInstance(ConverterProvider instance) {
        ConverterProvider.instance = instance;
    }
    
    private Map<Class<?>, Converter<?>> converterRegistry = Maps.newHashMap();
    
    public void register(Class<?> clazz, Class<? extends Converter<?>> converterClass) {
        Converter<?> converter = SquirrelProvider.getInstance().newInstance(converterClass);
        register(clazz, converter);
    }
    
    public void register(Class<?> clazz, Converter<?> converter) {
        converterRegistry.put(clazz, converter);
    }
    
    public void unregister(Class<?> clazz) {
        converterRegistry.remove(clazz);
    }
    
    @SuppressWarnings("unchecked")
    public <T> Converter<T> getConverter(Class<T> clazz) {
        return (Converter<T>)converterRegistry.get(clazz);
    }
}
