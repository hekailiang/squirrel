package org.squirrelframework.foundation.fsm;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

public class GeneralConverter<T> implements Converter<T> {

    private Class<T> type;

    public GeneralConverter(Class<T> type) {
        this.type = type;
    }

    @Override
    public String convertToString(T obj) {
        if(Enum.class.isAssignableFrom(type)) {
            return ((Enum<?>)obj).name();
        } else if(Date.class.isAssignableFrom(type)) {
            return Long.toString(((Date)obj).getTime());
        }
        return obj != null ? obj.toString() : StringUtils.EMPTY;
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public T convertFromString(String value) {
        if(value==null) return null;
        
        if (type.equals(boolean.class) || type.equals(Boolean.class)) {
            if (value == null || value.isEmpty()) {
                return null;
            } else if (value.equalsIgnoreCase("true")) {
                return type.cast(Boolean.TRUE);
            }
            return type.cast(Boolean.FALSE);
        } else if (Number.class.isAssignableFrom(type)
                || int.class.isAssignableFrom(type)
                || long.class.isAssignableFrom(type)
                || double.class.isAssignableFrom(type)
                || float.class.isAssignableFrom(type)
                || byte.class.isAssignableFrom(type)
                || short.class.isAssignableFrom(type)) {
            BigDecimal convertedValue = new BigDecimal(value);
            if (Integer.class.equals(type) || int.class.equals(type)) {
                return type.cast(convertedValue.intValue());
            } else if (Long.class.equals(type) || long.class.equals(type)) {
                return type.cast(convertedValue.longValue());
            } else if (Double.class.equals(type) || double.class.equals(type)) {
                return type.cast(convertedValue.doubleValue());
            } else if (Float.class.equals(type) || float.class.equals(type)) {
                return type.cast(convertedValue.floatValue());
            } else if (BigInteger.class.equals(type)) {
                return type.cast(convertedValue.toBigInteger());
            } else if (BigDecimal.class.equals(type)) {
                return type.cast(convertedValue);
            } else if (Short.class.equals(type) || short.class.equals(type)) {
                return type.cast(convertedValue.shortValue());
            } else if (Byte.class.equals(type) || byte.class.equals(type)) {
                return type.cast(convertedValue.byteValue());
            }
        } else if(Enum.class.isAssignableFrom(type)) {
            return type.cast(Enum.valueOf((Class)type, value));
        } else if(String.class.equals(type)) {
            return type.cast(value);
        } else if(Date.class.isAssignableFrom(type)) {
            return type.cast(new Date(Long.parseLong(value)));
        } else if(Character.class.equals(type) || char.class.equals(type)) {
            return type.cast(value.charAt(0));
        }
        throw new IllegalStateException("Unable to convert type: \'" +
                type.getName() +"\' with value \'"+value.toString()+"\'.");
    }

}
