package org.squirrelframework.foundation.fsm.jmx;

import java.lang.management.ManagementFactory;
import java.util.Hashtable;
import java.util.regex.Pattern;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.squirrelframework.foundation.fsm.StateMachine;
import org.squirrelframework.foundation.fsm.annotation.OnStateMachineStart;
import org.squirrelframework.foundation.fsm.annotation.OnStateMachineTerminate;

public class ManagementService {
    
    public static final String DOMAIN = "org.squirrelframework";
    
    public void register(StateMachine<?,?,?,?> fsm) {
        if(fsm.isRemoteMonitorEnabled()) {
            fsm.addDeclarativeListener(this);
        }
    }
    
    @OnStateMachineStart
    public void onStateMachineStart(StateMachine<?,?,?,?> fsm) {
        StateMachineManagementMBean mbean = new StateMachineManagement(fsm);
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName objectName = createObjectName(fsm);
        if (!mbs.isRegistered(objectName)) {
            try {
                mbs.registerMBean(mbean, objectName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    @OnStateMachineTerminate
    public void onStateMachineTerminate(StateMachine<?,?,?,?> fsm) {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName objectName = createObjectName(fsm);
        if (mbs.isRegistered(objectName)) {
            try {
                mbs.unregisterMBean(objectName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    protected ObjectName createObjectName(StateMachine<?,?,?,?> fsm){
        Hashtable<String, String> properties = new Hashtable<String, String>(2);
        properties.put("type", quote(fsm.getClass().getSimpleName()));
        properties.put("name", quote(fsm.getIdentifier()));
        try {
            return new ObjectName(DOMAIN, properties);
        } catch (MalformedObjectNameException e) {
            throw new IllegalArgumentException();
        }
    }
    
    public static String quote(String text){
        return Pattern.compile("[:\",=*?]").matcher(text)
                .find() ? ObjectName.quote(text) : text;
    }

}
