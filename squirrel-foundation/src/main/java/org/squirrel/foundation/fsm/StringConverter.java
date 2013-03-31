package org.squirrel.foundation.fsm;

import org.squirrel.foundation.component.SquirrelProvider;

public interface StringConverter extends Converter<String> {
    
    public static final Converter<String> INSTANCE = SquirrelProvider.getInstance().newInstance(StringConverterImpl.class);
    
    class StringConverterImpl implements StringConverter {
        
        private StringConverterImpl() {}

        @Override
        public String convertToString(String obj) {
            return obj;
        }

        @Override
        public String convertFromString(String name) {
            return name;
        }
    }
}
