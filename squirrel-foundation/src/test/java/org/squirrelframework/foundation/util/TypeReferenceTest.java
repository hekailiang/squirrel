package org.squirrelframework.foundation.util;

import com.google.common.reflect.TypeToken;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class TypeReferenceTest {

    @Test
    public void testArrayTypeDiscovery() {
        TypeReference<List<String>> typeReference = new TypeReference<List<String>>() {};

        assertEquals(typeReference.getType(), new TypeToken<List<String>>() {}.getType());
    }
}
