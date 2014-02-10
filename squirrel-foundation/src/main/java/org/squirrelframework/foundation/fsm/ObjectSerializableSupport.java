package org.squirrelframework.foundation.fsm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.squirrelframework.foundation.util.Base64Coder;

public abstract class ObjectSerializableSupport {
    
    public static String serialize(Object data) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            try {
                oos.writeObject(data);
            } finally {
                oos.close();
            }
            return new String(Base64Coder.encode(bos.toByteArray()));
        } catch (IOException e) {
            throw new RuntimeException("Data serialization failed.", e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T deserialize(String value) {
        try {
            byte [] data = Base64Coder.decode(value);
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
            try {
                Object object = ois.readObject();
                return (T)object;
            } finally {
                ois.close();
            }
        } catch (IOException e) {
            throw new RuntimeException("Data serialization failed.", e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
