package org.jstart.carrot.console.comm.entity;

import java.util.Objects;

public class KeyValue<T,M> {

    private T key;

    private M value;

    public KeyValue(){
    }

    public KeyValue(T key,M value){
        this.key = key;
        this.value = value;
    }

    public T getKey() {
        return key;
    }

    public M getValue() {
        return value;
    }

    public KeyValue<T,M> setValue(M value) {
        this.value = value;
        return this;
    }

    public KeyValue<T,M> setKey(T key) {
        this.key = key;
        return this;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        KeyValue<?, ?> keyValue = (KeyValue<?, ?>) object;
        return Objects.equals(key, keyValue.key) && Objects.equals(value, keyValue.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    @Override
    public String toString() {
        return "KeyValue{" +
                "key=" + key +
                ", value=" + value +
                '}';
    }
}
