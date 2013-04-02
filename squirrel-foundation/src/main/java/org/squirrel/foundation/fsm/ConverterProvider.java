package org.squirrel.foundation.fsm;

import org.squirrel.foundation.component.SquirrelComponent;
import org.squirrel.foundation.component.SquirrelProvider;
import org.squirrel.foundation.component.SquirrelSingleton;

public interface ConverterProvider extends SquirrelComponent, SquirrelSingleton {
	
	public static ConverterProvider INSTANCE = SquirrelProvider.getInstance().newInstance(ConverterProvider.class);
	
	void register(Class<?> clazz, Class<? extends Converter<?>> converterClass);
	
	void register(Class<?> clazz, Converter<?> converter);
	
	void unregister(Class<?> clazz);
	
	<T> Converter<T> getConverter(Class<T> clazz);
}
