package org.squirrelframework.foundation.component.impl;

import org.squirrelframework.foundation.component.PersonImpl;
import org.squirrelframework.foundation.component.Student;

public class StudentImpl extends PersonImpl implements Student {
    
    private String school;
    
    @Override
    public void setSchool(String school) {
        this.school = school;
    }

    @Override
    public String getSchool() {
        return school;
    }

}
