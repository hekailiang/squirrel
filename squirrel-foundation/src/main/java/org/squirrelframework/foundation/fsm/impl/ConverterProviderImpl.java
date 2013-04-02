package org.squirrelframework.foundation.fsm.impl;

import java.util.Map;

import org.squirrelframework.foundation.component.SquirrelProvider;
import org.squirrelframework.foundation.fsm.Converter;
import org.squirrelframework.foundation.fsm.ConverterProvider;

import com.google.common.collect.Maps;

public class ConverterProviderImpl implements ConverterProvider {
    
    private Map<Class<?>, Converter<?>> converterRegistry = Maps.newHashMap();
    
    @Override
    public void register(Class<?> clazz, Class<? extends Converter<?>> converterClass) {
        Converter<?> converter = SquirrelProvider.getInstance().newInstance(converterClass);
        register(clazz, converter);
    }
    
    @Override
    public void register(Class<?> clazz, Converter<?> converter) {
        converterRegistry.put(clazz, converter);
    }
    
    @Override
    public void unregister(Class<?> clazz) {
        converterRegistry.remove(clazz);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T> Converter<T> getConverter(Class<T> clazz) {
        return (Converter<T>)converterRegistry.get(clazz);
    }
}
