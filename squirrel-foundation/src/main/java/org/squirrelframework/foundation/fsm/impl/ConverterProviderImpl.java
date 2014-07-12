package org.squirrelframework.foundation.fsm.impl;

import com.google.common.collect.Maps;
import org.squirrelframework.foundation.component.SquirrelProvider;
import org.squirrelframework.foundation.fsm.Converter;
import org.squirrelframework.foundation.fsm.ConverterProvider;
import org.squirrelframework.foundation.fsm.GeneralConverter;

import java.util.Map;

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
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public <T> Converter<T> getConverter(Class<T> clazz) {
        Converter<T> converter = (Converter<T>)converterRegistry.get(clazz);
        if(converter==null) {
            converter = new GeneralConverter(clazz);
        }
        return converter;
    }

    @Override
    public void clearRegistry() {
        converterRegistry.clear();
    }
}
