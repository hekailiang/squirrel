package org.squirrelframework.foundation.fsm;

import org.squirrelframework.foundation.component.IdProvider;
import org.squirrelframework.foundation.component.SquirrelSingleton;

/**
 * This class is used to configure state machine runtime behavior
 * @author Henry.He
 */
public interface StateMachineConfiguration {
    
    boolean isAutoStartEnabled();
    
    void setAutoStartEnabled(boolean isAutoStartEnabled);
    
    boolean isAutoTerminateEnabled();
    
    void setAutoTerminateEnabled(boolean isAutoTerminateEnabled);
    
    boolean isDataIsolateEnabled();
    
    void setDataIsolateEnabled(boolean isDataIsolateEnabled);
    
    IdProvider getIdProvider();
    
    void setIdProvider(IdProvider idProvider);
    
    public class Default implements StateMachineConfiguration, SquirrelSingleton {
        
        private static StateMachineConfiguration instance = new Default();

        public static StateMachineConfiguration getInstance() {
            return instance;
        }

        public static void setInstance(StateMachineConfiguration instance) {
            Default.instance = instance;
        }
        
        private boolean isAutoStartEnabled = true;
        
        private boolean isAutoTerminateEnabled = true;
        
        private boolean isDataIsolateEnabled = false;
        
        private IdProvider idProvider = IdProvider.Default.getInstance();
        
        @Override
        public boolean isAutoStartEnabled() {
            return isAutoStartEnabled;
        }

        @Override
        public void setAutoStartEnabled(boolean isAutoStartEnabled) {
            this.isAutoStartEnabled = isAutoStartEnabled;
        }

        @Override
        public boolean isAutoTerminateEnabled() {
            return isAutoTerminateEnabled;
        }

        @Override
        public void setAutoTerminateEnabled(boolean isAutoTerminateEnabled) {
            this.isAutoTerminateEnabled = isAutoTerminateEnabled;
        }

        @Override
        public boolean isDataIsolateEnabled() {
            return isDataIsolateEnabled;
        }

        @Override
        public void setDataIsolateEnabled(boolean isDataIsolateEnabled) {
            this.isDataIsolateEnabled = isDataIsolateEnabled;
        }

        @Override
        public IdProvider getIdProvider() {
            return idProvider;
        }

        @Override
        public void setIdProvider(IdProvider idProvider) {
            this.idProvider = idProvider;
        }
        
    }
}
