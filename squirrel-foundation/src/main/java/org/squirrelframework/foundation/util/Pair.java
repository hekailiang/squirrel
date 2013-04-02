package org.squirrelframework.foundation.util;

public class Pair<F, S> {
    private final F first;
    private final S second;

    public Pair(F a, S b) {
        this.first = a;
        this.second = b;
    }

    public F first() {
        return first;
    }

    public S second() {
        return second;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Pair)) {
            return false;
        }
        Pair<?, ?> other = (Pair<?, ?>) o;
        return first.equals(other.first) && second.equals(other.second);
    }
    
    @Override
    public int hashCode() {
        return first.hashCode() * 13 + second.hashCode() * 7;
    }
    
    @Override
    public String toString() {
        return first + ":" + second;
    }
}