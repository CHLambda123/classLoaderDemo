package com.lambda.myimplement;

import com.lambda.myinterface.InterfaceDemo;

public class MyImplementDemo implements InterfaceDemo {
    @Override
    public void sayWhoAreYou() {
        System.out.println("I am Version B");
    }
}
