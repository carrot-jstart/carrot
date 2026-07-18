package org.jstart.carrot.console.comm.validator;

public class ParameterRunnable<T> implements Runnable {
    T param;
    public ParameterRunnable(T param) {
        this.param = param;
    }

    @Override
    public void run() {
        new ParameterRunnable(param);
    }
}
