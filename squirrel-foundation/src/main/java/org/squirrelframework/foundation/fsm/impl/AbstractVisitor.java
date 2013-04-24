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
}
