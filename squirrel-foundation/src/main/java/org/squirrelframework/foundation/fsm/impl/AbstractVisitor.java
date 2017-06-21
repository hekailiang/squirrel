package org.squirrelframework.foundation.fsm.impl;

import java.io.FileWriter;
import java.io.IOException;

abstract class AbstractVisitor {

    protected final StringBuilder buffer = new StringBuilder();

    protected void writeLine(final String msg) {
        buffer.append(msg).append("\n");
    }

    protected String quoteName(final String id) {
        return "\"" + id + "\"";
    }

    protected void saveFile(final String filename, String content) {
        try {
            FileWriter file = new FileWriter(filename);
            file.write(content);
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * get enum name instead of toString value
     *
     * @param enumObj
     * @return
     */
    protected static String getName(Object enumObj) {
        String stateValue;
        if (enumObj.getClass().isEnum()) {
            try {
                stateValue = (String)enumObj.getClass().getMethod("name").invoke(enumObj, null);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        } else {
            stateValue = enumObj.toString();
        }
        return stateValue;
    }

    /**
     * quote with enum name value
     *
     * @param enumObj
     * @return
     */
    protected String quoteEnumName(final Object enumObj) {
        return quoteName(getName(enumObj));
    }
}
