package org.squirrel.foundation.component.impl;

import org.squirrel.foundation.component.PersonImpl;
import org.squirrel.foundation.component.Student;

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
