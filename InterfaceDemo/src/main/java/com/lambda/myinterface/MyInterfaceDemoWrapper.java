package com.lambda.myinterface;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MyInterfaceDemoWrapper {
    private Object myImplementDemo;
    private ConcurrentMap<String, Method> map = new ConcurrentHashMap<>();
    public MyInterfaceDemoWrapper(Object myImplementDemo) {
        this.myImplementDemo = myImplementDemo;
        Method[] methods = this.myImplementDemo.getClass().getMethods();
        for (Method method : methods) {
            map.put(method.getName(), method);
        }
    }
    public void sayWhoAreYou() {
        try {
            map.get(Thread.currentThread().getStackTrace()[1].getMethodName()).invoke(myImplementDemo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
