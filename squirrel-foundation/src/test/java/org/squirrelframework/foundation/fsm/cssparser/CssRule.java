package org.squirrelframework.foundation.fsm.cssparser;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

public class CssRule {
    
    private String selector;
    
    private List<CssProperty> properties;
    
    public String getSelector() {
        return selector;
    }
    
    public void setSelector(String selector) {
        this.selector = selector;
    }
    
    public List<CssProperty> getProperties() {
        if(properties==null) 
            return Collections.emptyList();
        return properties;
    }
    
    public void addProperty(CssProperty property) {
        if(properties==null)
            properties = Lists.newArrayList();
        properties.add(property);
    }
    
    public CssProperty getProperty(String name) {
        for(CssProperty p : properties) {
            if(p.getName().equals(name)) 
                return p;
        }
        return null;
    }
}
