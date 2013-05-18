package org.squirrelframework.foundation.fsm;

import java.util.List;
/**
 * Constants conditions
 * 
 * @author Henry.He
 *
 */
public class Conditions {
    
    public static class Always<C> implements Condition<C> {
        @Override
        public boolean isSatisfied(C context) {
            return true;
        }
    }
    
    public static <C> Condition<C> always()  {
        return new Always<C>();
    }
    
    public static class Never<C> implements Condition<C> {
        @Override
        public boolean isSatisfied(C context) {
            return false;
        }
    }
    
    public static <C> Condition<C> never() {
        return new Never<C>();
    }

    public static <C> Condition<C> and(final Condition<C> first, final Condition<C> second) {
        return new Condition<C>() {
            @Override
            public boolean isSatisfied(C context) {
                return first.isSatisfied(context) && second.isSatisfied(context);
            }
        };
    }

    public static <C> Condition<C> and(final List<Condition<C>> conditions) {
        return new Condition<C>() {
            @Override
            public boolean isSatisfied(C context) {
                for (Condition<C> condition : conditions) {
                    if (!condition.isSatisfied(context)) {
                        return false;
                    }
                }
                return true;
            }
        };
    }

    public static <C> Condition<C> or(final Condition<C> first, final Condition<C> second) {
        return new Condition<C>() {
            @Override
            public boolean isSatisfied(C context) {
                return first.isSatisfied(context) || second.isSatisfied(context);
            }
        };
    }

    public static <C> Condition<C> or(final List<Condition<C>> conditions) {
        return new Condition<C>() {
            @Override
            public boolean isSatisfied(C context) {
                for (Condition<C> condition : conditions) {
                    if (condition.isSatisfied(context)) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    public static <C> Condition<C> not(final Condition<C> condition) {
        return new Condition<C>() {
            @Override
            public boolean isSatisfied(C context) {
                return !condition.isSatisfied(context);
            }
        };
    }

    public static <C> Condition<C> xor(final Condition<C> first, final Condition<C> second) {
        return new Condition<C>() {
            @Override
            public boolean isSatisfied(C context) {
                return first.isSatisfied(context) ^ second.isSatisfied(context);
            }
        };
    }
}
