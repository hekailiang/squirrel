package org.squirrelframework.foundation.fsm;

import org.squirrelframework.foundation.component.SquirrelComponent;
import org.squirrelframework.foundation.component.SquirrelProvider;
import org.squirrelframework.foundation.component.SquirrelSingleton;

public interface ConverterProvider extends SquirrelComponent, SquirrelSingleton {
	
	public static ConverterProvider INSTANCE = SquirrelProvider.getInstance().newInstance(ConverterProvider.class);
	
	void register(Class<?> clazz, Class<? extends Converter<?>> converterClass);
	
	void register(Class<?> clazz, Converter<?> converter);
	
	void unregister(Class<?> clazz);
	
	void clearRegistry();
	
	<T> Converter<T> getConverter(Class<T> clazz);
}
