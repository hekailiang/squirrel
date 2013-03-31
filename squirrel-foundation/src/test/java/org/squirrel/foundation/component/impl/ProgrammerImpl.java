package org.squirrel.foundation.component.impl;

import javax.annotation.PostConstruct;

import org.squirrel.foundation.component.PersonImpl;
import org.squirrel.foundation.component.Programmer;

public class ProgrammerImpl extends PersonImpl implements Programmer {
    
    private String lang;
    
    @PostConstruct
    public void postConstruct() {
        setName("Henry");
        lang = "Java";
    }

    @Override
    public String getLanguage() {
        return lang;
    }
}
